package com.dili.ss.redis.delayqueue.task;

import com.alibaba.fastjson.JSON;
import com.dili.ss.component.CustomThreadPoolExecutor;
import com.dili.ss.redis.delayqueue.annotation.StreamListener;
import com.dili.ss.redis.delayqueue.component.BeanMethodCacheComponent;
import com.dili.ss.redis.delayqueue.consts.DelayQueueConstants;
import com.dili.ss.redis.delayqueue.dto.DelayMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 分布式延时队列任务处理器
 * @author asiamaster
 * date 2021-01-27
 */
@Component
@ConditionalOnExpression("'${ss.delayqueue.distributed.enable}'=='true'")
public class HandleTask {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ApplicationContext applicationContext;
    // key为bean， value为@StreamListener注解的method
    private Map<Object, Method> map = new HashMap<>();

    @Resource
    BeanMethodCacheComponent beanMethodCacheComponent;
    @Resource
    private CustomThreadPoolExecutor customThreadPoolExecutor;

    /**
     * 每3秒执行一次
     */
    @Scheduled(cron = "${ss.handleTask.scheduled:0/3 * * * * ?}")
    public void scheduledTask() {
        try {
            Set<String> activeTopics = redisTemplate.opsForSet().members(DelayQueueConstants.META_TOPIC_ACTIVE);
            Map<Object, Method> beanMethod = beanMethodCacheComponent.getBeanMethod(StreamListener.class);
            for (String activeTopic : activeTopics) {
                if (!redisTemplate.hasKey(activeTopic)) {
                    // 如果 KEY 不存在元数据中删除
                    redisTemplate.opsForSet().remove(DelayQueueConstants.META_TOPIC_ACTIVE, activeTopic);
                    continue;
                }
                //这句代码有缺陷，一次只能捞一条出来，有多条数据只能等@Scheduled注解的下一个周期
                String delayMessageJson = redisTemplate.opsForList().leftPop(activeTopic);
                while (StringUtils.isNotBlank(delayMessageJson)) {
                    for (Map.Entry<Object, Method> entry : beanMethod.entrySet()) {
                        DelayMessage message = JSON.parseObject(delayMessageJson, DelayMessage.class);
                        StreamListener streamListener = entry.getValue().getAnnotation(StreamListener.class);
                        if(!streamListener.value().equals(message.getTopic())){
                            continue;
                        }
                        String finalDelayMessageJson = delayMessageJson;
                        customThreadPoolExecutor.getExecutor(DelayQueueConstants.DELAY_QUEUE_EXECUTOR_KEY).submit(() -> {
                            try {
                                logger.debug("消息到期执行({})", message.getTopic());
                                entry.getValue().invoke(entry.getKey(), message);
                            } catch (Throwable t) {
                                // 失败重新放入失败队列
                                String failKey = activeTopic.replace(DelayQueueConstants.DELAY_ACTIVE_KEY, DelayQueueConstants.DELAY_FAIL_KEY);
                                redisTemplate.opsForList().rightPush(failKey, finalDelayMessageJson);
                                logger.warn("消息监听器发送异常: ", t);
                            }
                        });
                        break;
                    }
                    delayMessageJson = redisTemplate.opsForList().leftPop(activeTopic);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
