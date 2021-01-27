package com.dili.ss.redis.delayqueue.task;

import com.alibaba.fastjson.JSON;
import com.dili.ss.redis.delayqueue.DelayMessage;
import com.dili.ss.redis.delayqueue.annotation.StreamListener;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dili.ss.redis.delayqueue.RedisDelayQueue.META_TOPIC;

/**
 * 单实例版消息出列处理器
 */
@Component
public class StandaloneTopicDequeueTask {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ApplicationContext applicationContext;
    private ExecutorService executors = Executors.newFixedThreadPool(8);
    // key为bean， value为@StreamListener注解的method
    private Map<Object, Method> map = new HashMap<>();

    /**
     * 每秒执行一次
     */
    @Scheduled(cron = "${ss.standaloneTopicDequeueTask.scheduled:0/1 * * * * ?}")
    public void scheduledTask() {
        try {
            //获取所有的topic，再根据topic查询已到期的
            Set<String> topics = redisTemplate.opsForSet().members(META_TOPIC);
            Map<Object, Method> map = getBean(StreamListener.class);
            for (String topic : topics) {
                if (!redisTemplate.hasKey(topic)) {
                    // 如果 KEY 不存在元数据中删除
                    redisTemplate.opsForSet().remove(META_TOPIC, topic);
                    continue;
                }
                Long startTime = System.currentTimeMillis();

                Set<String> sets = redisTemplate.opsForZSet().rangeByScore(topic, 0, startTime);
                if(sets.isEmpty()){
                    continue;
                }
                try {
                    Iterator<String> iterator = sets.iterator();
                    String delayMessageJson = null;
                    while (iterator.hasNext()) {
                        delayMessageJson = iterator.next();
                        for (Map.Entry<Object, Method> entry : map.entrySet()) {
                            DelayMessage message = JSON.parseObject(delayMessageJson, DelayMessage.class);
                            StreamListener streamListener = entry.getValue().getAnnotation(StreamListener.class);
                            if (!streamListener.value().equals(message.getTopic())) {
                                continue;
                            }
                            String finalDelayMessageJson = delayMessageJson;
                            executors.submit(() -> {
                                try {
                                    entry.getValue().invoke(entry.getKey(), message);
                                } catch (Throwable t) {
                                    // 失败重新放入失败队列
//                                String failKey = topic.replace("delay:active", "delay:fail");
//                                redisTemplate.opsForList().rightPush(failKey, finalDelayMessageJson);
                                    logger.warn("延时队列任务处理异常: ", t);
                                }
                            });
                            logger.info("消息到期发送到消息监听器, topic: {}", message.getTopic());
                            //当前消息被处理后就退出，不能再让其它的StreamListener处理
                            break;
                        }
                    }
                }finally {
                    redisTemplate.opsForZSet().removeRangeByScore(topic, 0, startTime);
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
