package com.dili.ss.redis.delayqueue.task;

import com.alibaba.fastjson.JSON;
import com.dili.ss.component.CustomThreadPoolExecutor;
import com.dili.ss.redis.delayqueue.DelayMessage;
import com.dili.ss.redis.delayqueue.annotation.StreamListener;
import com.dili.ss.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.dili.ss.redis.delayqueue.RedisDelayQueue.META_TOPIC_ACTIVE;

/**
 * 分布式延时队列任务处理器
 */
@Component
public class HandleTask {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ApplicationContext applicationContext;
    // key为bean， value为@StreamListener注解的method
    private Map<Object, Method> map = new HashMap<>();

    //    多线程执行器
    @Resource
    private CustomThreadPoolExecutor customThreadPoolExecutor;

    /**
     * 每3秒执行一次
     */
    @Scheduled(cron = "${ss.handleTask.scheduled:0/3 * * * * ?}")
    public void scheduledTask() {
        try {
            Set<String> activeTopics = redisTemplate.opsForSet().members(META_TOPIC_ACTIVE);
            Map<Object, Method> map = getBean(StreamListener.class);
            for (String activeTopic : activeTopics) {
                if (!redisTemplate.hasKey(activeTopic)) {
                    // 如果 KEY 不存在元数据中删除
                    redisTemplate.opsForSet().remove(META_TOPIC_ACTIVE, activeTopic);
                    continue;
                }
                //这句代码有缺陷，一次只能捞一条出来，有多条数据只能等@Scheduled注解的下一个周期
                String delayMessageJson = redisTemplate.opsForList().leftPop(activeTopic);
                while (StringUtils.isNotBlank(delayMessageJson)) {
                    for (Map.Entry<Object, Method> entry : map.entrySet()) {
                        DelayMessage message = JSON.parseObject(delayMessageJson, DelayMessage.class);
                        StreamListener streamListener = entry.getValue().getAnnotation(StreamListener.class);
                        if(!streamListener.value().equals(message.getTopic())){
                            continue;
                        }
                        String finalDelayMessageJson = delayMessageJson;
                        customThreadPoolExecutor.getExecutor().submit(() -> {
                            try {
                                logger.info(DateUtils.format(new Date())+",处理消息:"+finalDelayMessageJson);
                                entry.getValue().invoke(entry.getKey(), message);
                            } catch (Throwable t) {
                                // 失败重新放入失败队列
                                String failKey = activeTopic.replace("delay:active", "delay:fail");
                                redisTemplate.opsForList().rightPush(failKey, finalDelayMessageJson);
                                logger.warn("延迟队列[3]，消息监听器发送异常: ", t);
                            }
                        });
                        logger.info("延迟队列[3]，消息到期发送到消息监听器: {}", message.getTopic());
                        break;
                    }
                    delayMessageJson = redisTemplate.opsForList().leftPop(activeTopic);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 构建key为bean， value为@StreamListener注解的method的map
     * @param annotationClass
     * @return
     */
    private Map<Object, Method> getBean(Class<? extends Annotation> annotationClass) {
        if (!this.map.isEmpty()) {
            return this.map;
        }
        Map<Object, Method> map = new HashMap<>();
        String[] beans = applicationContext.getBeanDefinitionNames();
        for (String beanName : beans) {
            Class<?> clazz = applicationContext.getType(beanName);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                boolean present = method.isAnnotationPresent(annotationClass);
                if (present) {
                    map.put(applicationContext.getBean(beanName), method);
                    break;
                }
            }
        }
        this.map = map;
        return map;
    }
}
