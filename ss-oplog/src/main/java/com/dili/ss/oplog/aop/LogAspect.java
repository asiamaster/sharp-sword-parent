package com.dili.ss.oplog.aop;

import com.dili.ss.dto.IDTO;
import com.dili.ss.exception.ParamErrorException;
import com.dili.ss.java.B;
import com.dili.ss.oplog.annotation.LogParam;
import com.dili.ss.oplog.annotation.OpLog;
import com.dili.ss.oplog.base.LogContentProvider;
import com.dili.ss.oplog.base.LogHandler;
import com.dili.ss.oplog.base.LogInitializer;
import com.dili.ss.oplog.dto.LogContext;
import com.dili.ss.util.BeanConver;
import com.dili.ss.util.ICustomThreadPoolExecutor;
import com.dili.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 日志切面
 */
@Component
@Aspect
@DependsOn("initConfig")
@ConditionalOnExpression("'${oplog.enable}'=='true'")
public class LogAspect {

    @Resource(name="StringGroupTemplate")
    GroupTemplate groupTemplate;
    //配置默认的内容提供者
    @Value("${oplog.contentProvider:}")
    private String contentProvider;
    //配置默认的日志处理者
    @Value("${oplog.handler:}")
    private String handler;
    //配置默认的日志初始化器
    @Value("${oplog.initializer:}")
    private String initializer;

    private ExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    //LogHandler的缓存
    Map<String, LogHandler> loghandlerCache = new HashMap<>();
    //日志初始化器的缓存
    Map<String, LogInitializer> logInitializerCache = new HashMap<>();
    @PostConstruct
    public void init() throws IllegalAccessException, InstantiationException {
        System.out.println("操作日志启动");
        executor = ((Class<ICustomThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor();
    }

    /**
     * 设置token
     * @param point
     * @return
     * @throws Throwable
     */
    @Around( "@annotation(com.dili.ss.oplog.annotation.OpLog)")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        LogContext logContext = null;
        //初始化LogContext
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);
        //日志初始化器
        String initializer = StringUtils.isBlank(opLog.initializer()) ? this.initializer : opLog.initializer();
        if(StringUtils.isNotBlank(initializer)){
            if(!logInitializerCache.containsKey(initializer)){
                LogInitializer logInitializer = getObj(initializer, LogInitializer.class);
                logInitializerCache.put(initializer, logInitializer);
            }
            LogInitializer logInitializer = logInitializerCache.get(initializer);
            if(logInitializer != null){
                logContext = logInitializer.init(((MethodSignature) point.getSignature()).getMethod(), point.getArgs());
            }
        }
        LogContext finalLogContext = logContext;
        String content = getContent(point, finalLogContext);
        //先执行方法
        Object retValue = point.proceed();
        //方法执行完后异步执行日志
        executor.execute(() -> {
            try {
                log(point, finalLogContext, content);
            } catch (Exception e) {
                log.error("操作日志异常:"+e.getMessage());
            }
        });
        return retValue;
    }

    /**
     * 获取内容
     * @param point
     * @param logContext
     * @return
     */
    private String getContent(ProceedingJoinPoint point, LogContext logContext){
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);
        String content = null;
        //内容处理器
        String cp = StringUtils.isBlank(opLog.contentProvider()) ? contentProvider : opLog.contentProvider();
        //如果有自定义内容处理器
        if(StringUtils.isNotBlank(cp)){
            LogContentProvider logContentProvider = null;
            try {
                logContentProvider = getObj(cp, LogContentProvider.class);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | BeansException e) {
                log.error(e.getMessage());
                return null;
            }
            content = logContentProvider.content(method, point.getArgs(), opLog.params(), logContext);
        }else{
            content = getBeetlContent(method, point.getArgs());
        }
        return content;
    }

    /**
     * 记录日志
     * @param point
     * @param logContext 上下文
     * @param content 日志内容
     * @throws ClassNotFoundException
     */
    private void log(ProceedingJoinPoint point, LogContext logContext, String content) throws Exception {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);
        String handler = StringUtils.isBlank(opLog.handler()) ? this.handler : opLog.handler();
        //有handler并且有内容
        if(StringUtils.isNotBlank(handler)) {
            LogHandler logHandler = getLogHandler(handler);
            if(logHandler != null){
                logHandler.log(content, method, point.getArgs(), opLog.params(), logContext);
            }
        }
    }

    /**
     * 获取beetl模板内容
     * @param method
     * @param args
     * @return
     */
    private String getBeetlContent(Method method, Object[] args){
        OpLog opLog = method.getAnnotation(OpLog.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        //取方法上OpLog注解的模板
        String tpl = StringUtils.isBlank(opLog.value()) ? opLog.template() : opLog.value();
        if(StringUtils.isBlank(tpl)){
            log.warn("日志模板为空");
            return null;
        }
        //如果有必填表达式，在模板头尾加上验证
        if(StringUtils.isNotBlank(opLog.requiredExpr())){
            String head = "<% if(" + opLog.requiredExpr() + "){%>";
            String foot = "<%}%>";
            tpl = head + tpl + foot;
        }
        Template template = groupTemplate.getTemplate(tpl);
        Map<String, Object> params = null;
        try {
            //获取模板绑定变量
            params = getBindingMap(parameterAnnotations, parameterTypes, args);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        //绑定参数中的变量
        template.binding(params);
        //模板检验失败则打印日志
        BeetlException exception = template.validate();
        if(exception != null){
            log.error(exception.getMessage());
            return null;
        }
        return template.render();
    }

    /**
     * 获取日志处理器
     * @param logHandler
     * @return
     * @throws ClassNotFoundException
     */
    private LogHandler getLogHandler(String logHandler) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!loghandlerCache.containsKey(logHandler)){
            LogHandler logHandlerInstance = null;
            try {
                logHandlerInstance = getObj(logHandler, LogHandler.class);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | BeansException e) {
                log.error(e.getMessage());
                return null;
            }
            loghandlerCache.put(logHandler, logHandlerInstance);
        }
        return loghandlerCache.get(logHandler);
    }

    /**
     * 根据名称取对象
     * 反射或取spring bean
     * @param objName
     * @return
     */
    private <T> T getObj(String objName, Class<T> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException, BeansException {
        if(objName.contains(".")){
            Class objClass = Class.forName(objName);
            if(clazz.isAssignableFrom(objClass)){
                return (T) objClass.newInstance();
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }else{
            T bean = null;
            try {
                //这里可能bean不存在，会抛异常
                bean = SpringUtil.getBean(objName, clazz);
            } catch (BeansException e) {
                throw e;
            }
            if(clazz.isAssignableFrom(bean.getClass())){
                return bean;
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }
    }

    /**
     * 获取模板绑定变量
     * @param parameterAnnotations
     * @param parameterTypes
     * @param args
     * @return
     * @throws Exception
     */
    private Map<String, Object> getBindingMap(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes, Object[] args) throws Exception {
        //记录必填验证失败的绑定变量名称
        String inValidBindName = null;
        //临时记录绑定变量
        Map<String, Object> params = new HashMap<>();
        //循环设置每个方法形参的绑定变量
        for(int i = 0; i < parameterAnnotations.length && StringUtils.isBlank(inValidBindName); i++){
            Annotation[] annotations = parameterAnnotations[i];
            for(Annotation annotation : annotations){
                if(annotation instanceof LogParam){
                    if(((LogParam) annotation).required() && args[i] == null){
                        String bindName = ((LogParam) annotation).value();
                        inValidBindName = StringUtils.isBlank(bindName) ? ((LogParam) annotation).bindName() : bindName;
                        break;
                    }
                    String bindName = ((LogParam) annotation).bindName();
                    //List和数组如果没有bindName，默认为list
                    if(List.class.isAssignableFrom(parameterTypes[i]) || parameterTypes[i].isArray()){
                        if(StringUtils.isBlank(bindName)){
                            bindName = "list";
                        }
                        params.put(bindName, args[i]);
                    }
                    //DTO、JavaBean和Map可以没有bindName
                    else if(StringUtils.isBlank(bindName) && IDTO.class.isAssignableFrom(parameterTypes[i])){
                        params.putAll(BeanConver.transformObjectToMap(args[i]));
                    }else if(StringUtils.isBlank(bindName) && Map.class.isAssignableFrom(parameterTypes[i])){
                        params.putAll((Map)args[i]);
                    }else{
                        if(StringUtils.isBlank(bindName)){
                            bindName = "args["+i+"]";
                        }
                        if(null != args[i]) {
                            params.put(bindName, args[i]);
                        }
                    }
                }
            }
        }
        if(StringUtils.isNotBlank(inValidBindName)){
            throw new ParamErrorException("必填参数["+inValidBindName + "]为空");
        }
        return params;
    }


}
