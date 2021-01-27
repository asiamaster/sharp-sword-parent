package com.dili.ss.redis.delayqueue.impl;

import com.alibaba.fastjson.JSON;
import com.dili.ss.redis.delayqueue.DelayMessage;
import com.dili.ss.redis.delayqueue.RedisDelayQueue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 单机版延迟队列实现
 * 不支持多实例
 * @author wm
 * @date 2021-01-26
 */
@Component
public class StandaloneRedisDelayQueueImpl<E extends DelayMessage> implements RedisDelayQueue<E> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Override
    public void poll() {
        // todo
    }

    /**
     * 发送消息
     *
     * @param e
     */
    @Override
    public void push(E e) {
        try {
            String jsonStr = JSON.toJSONString(e);
            String topic = e.getTopic();
            String zkey = String.format("delay:wait:%s", topic);
//            Boolean result = redisTemplate.opsForZSet().add(zkey, jsonStr, e.getDelayTime());
            String script = "redis.call('sadd', KEYS[1], ARGV[1])\n" +
                            "redis.call('zadd', KEYS[2], ARGV[2], ARGV[3])\n" +
                            "return 1";
            Object[] keys = new Object[]{serialize(META_TOPIC), serialize(zkey)};
            Object[] values = new Object[]{ serialize(zkey), serialize(String.valueOf(e.getDelayTime())), serialize(jsonStr)};

            Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof RedisAsyncCommands) {
                    RedisAsyncCommands commands = (RedisAsyncCommands) nativeConnection;
                    return (Long) commands.getStatefulConnection().sync().eval(script, ScriptOutputType.INTEGER, keys, values);
                } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                    RedisAdvancedClusterAsyncCommands commands = (RedisAdvancedClusterAsyncCommands) nativeConnection;
                    return (Long) commands.getStatefulConnection().sync().eval(script, ScriptOutputType.INTEGER, keys, values);
                }
                return 0L;
            });
            if(result != null && result > 0) {
                logger.info("消息推送成功进入延时队列, topic: {}", e.getTopic());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * lettuce连接包下序列化键值，否则无法用默认的ByteArrayCodec解析
     * @param key
     * @return
     */
    private byte[] serialize(String key) {
        RedisSerializer<String> stringRedisSerializer =
                (RedisSerializer<String>) redisTemplate.getKeySerializer();
        return stringRedisSerializer.serialize(key);
    }

}
