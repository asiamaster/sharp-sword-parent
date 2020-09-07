package com.dili.ss.idempotent.aop;

import com.dili.ss.idempotent.service.IdempotentTokenService;
import com.dili.ss.redis.service.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 幂等切面
 */
@Component
@Aspect
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentAspect {
    @Autowired
    IdempotentTokenService idempotentTokenService;

    @Autowired
    RedisDistributedLock redisDistributedLock;

    IdempotentAspectHandler idempotentAspectHandler;
    @PostConstruct
    public void init() {
        idempotentAspectHandler = new IdempotentAspectHandlerImpl();
    }

    /**
     * 设置token
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.dili.ss.idempotent.annotation.Token)")
    public Object token(ProceedingJoinPoint point) throws Throwable {
        return idempotentAspectHandler.aroundToken(point, idempotentTokenService);
    }

    /**
     * 幂等验证
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.dili.ss.idempotent.annotation.Idempotent)")
    public Object idempotent(ProceedingJoinPoint point) throws Throwable {
        return idempotentAspectHandler.aroundIdempotent(point, redisDistributedLock);
    }

}
