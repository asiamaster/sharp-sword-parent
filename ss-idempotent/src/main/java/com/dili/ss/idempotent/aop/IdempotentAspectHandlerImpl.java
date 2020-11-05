package com.dili.ss.idempotent.aop;

import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.idempotent.annotation.Idempotent;
import com.dili.ss.idempotent.annotation.Token;
import com.dili.ss.idempotent.service.IdempotentTokenService;
import com.dili.ss.redis.service.RedisDistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author asiam
 */
public class IdempotentAspectHandlerImpl implements IdempotentAspectHandler{

    public static final String TOKEN_VALUE = "token_value";

    @Override
    public Object aroundIdempotent(ProceedingJoinPoint point, RedisDistributedLock redisDistributedLock) throws Throwable{
        Signature signature = point.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Idempotent idempotent = currentMethod.getAnnotation(Idempotent.class);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String type = StringUtils.isBlank(idempotent.value()) ? idempotent.type() : idempotent.value();
        String tokenValue = type.equals(Idempotent.HEADER) ? request.getHeader(TOKEN_VALUE) : request.getParameter(TOKEN_VALUE);
        //当大量高并发下所有带token参数的请求进来时，进行分布式锁定,允许某一台服务器的一个线程进入，锁定时间3分钟
        if (redisDistributedLock.tryGetLock(tokenValue,tokenValue,180L)) {
            if (redisDistributedLock.exists(request.getRequestURI() + tokenValue)) {
                //当请求的url与token与redis中的存储相同时
                if (redisDistributedLock.get(request.getRequestURI() + tokenValue).equals(tokenValue)) {
                    //放行的该线程删除redis中存储的token
                    redisDistributedLock.remove(request.getRequestURI() + tokenValue);
                    //放行
                    try {
                        return point.proceed();
                    }finally {
                        //完成后释放锁
                        if (redisDistributedLock.exists(tokenValue)) {
                            redisDistributedLock.releaseLock(tokenValue, tokenValue);
                        }
                    }
                }
            }
            //当请求的url与token与redis中的存储不相同时，解除锁定
            redisDistributedLock.releaseLock(tokenValue, tokenValue);
        }
        //进行拦截
        return BaseOutput.class.isAssignableFrom(currentMethod.getReturnType()) ? BaseOutput.failure(ResultCode.IDEMPOTENT_ERROR, "幂等接口调用失败") : null;
    }

    @Override
    public Object aroundToken(ProceedingJoinPoint point, IdempotentTokenService idempotentTokenService) throws Throwable{
        Signature signature = point.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Token token = currentMethod.getAnnotation(Token.class);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if(StringUtils.isNotBlank(token.value())){
            request.setAttribute(TOKEN_VALUE, idempotentTokenService.getToken(token.value()).getValue());
        }else if(StringUtils.isNotBlank(token.url())){
            request.setAttribute(TOKEN_VALUE, idempotentTokenService.getToken(token.url()).getValue());
        }else{
            //value和url都为空，不进入页面
            return false;
        }
        return point.proceed();
    }
}
