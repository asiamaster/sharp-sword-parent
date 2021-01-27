package com.dili.ss.redis.delayqueue.task;

import com.dili.ss.redis.delayqueue.consts.DelayQueueConstants;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;


/**
 * 分布式延时队列分发任务，先把数据捞出来，扔到处理队列
 *
 */
@Component
public class DistributeTask {

    private static final String LUA_SCRIPT;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    static {
        //屏蔽参考的lua脚本
//        StringBuilder sb = new StringBuilder(256);
//        //返回有序集合KEYS[1]:(delay:wait:topic)中小于ARGV[1]:System.currentTimeMillis()score区间的成员(DelayMessage.body)，只取一条(就像SQL中的 SELECT LIMIT offset, count )
////        sb.append("local val = redis.call('zrangebyscore', KEYS[1], '-inf', ARGV[1], 'limit', 0, 100)\n");
//        sb.append("local val = redis.call('zrangebyscore', KEYS[1], '-inf', ARGV[1])\n");
//        sb.append("if(next(val) ~= nil) then\n");
//        //向META_TOPIC_ACTIVE(zset)集合中添加topicKey(delay:active:topic)
//        sb.append("    redis.call('sadd', KEYS[2], ARGV[2])\n");
//        //移除有序集合KEYS[1](delay:wait:topic)中，下标为0到val数组长度-1区间的数据
//        sb.append("    redis.call('zremrangebyrank', KEYS[1], 0, #val - 1)\n");
//        sb.append("    for i = 1, #val, 100 do\n");
////        sb.append("redis.call('sadd', 'delay:i', tostring(i))\n");
////        sb.append("redis.call('sadd', 'delay:#val', tostring(#val))\n");
//        //向KEYS[3]:topicKey(delay:active:topic)列表中循环插入所有数组元素
//        //unpack()函数是接受一个数组来作为输入参数，并默认从下标为1开始返回所有元素。
//        sb.append("        redis.call('rpush', KEYS[3], unpack(val, i, math.min(i+99, #val)))\n");
//        sb.append("    end\n");
//        sb.append("    return 1\n");
//        sb.append("end\n");
//        sb.append("return 0");

        //长度288
        StringBuilder sb = new StringBuilder(512);
        // ----------   注释说明参数  ----------
        //KEY1:delay:wait:testTopic, KEY2:META_TOPIC_ACTIVE, KEY3:delay:active:testTopic
        //ARGV1:System.currentTimeMillis(), ARGV2:delay:active:testTopic
        //------------------------------------------------------------------------------------
        //返回有序集合KEYS[1]:(delay:wait:topic)中小于ARGV[1]:System.currentTimeMillis()score区间的成员(DelayMessage.body)，只取一条(就像SQL中的 SELECT LIMIT offset, count )
        sb.append("local val = redis.call('zrangebyscore', KEYS[1], '-inf', ARGV[1])\n");
//        sb.append("redis.call('sadd', 'delay:val:length', '长度:'..tostring(#val))\n");
        sb.append("if(next(val) ~= nil) then\n");
        //向META_TOPIC_ACTIVE(zset)集合中添加topicKey(delay:active:topic)
        sb.append("    redis.call('sadd', KEYS[2], ARGV[2])\n");
        //移除有序集合KEYS[1](delay:wait:topic)中zrangebyscore取出的数据
        sb.append("    redis.call('zremrangebyscore', KEYS[1], '-inf', ARGV[1])\n");
        //向KEYS[3]:topicKey(delay:active:topic)列表中循环插入所有数组元素
        sb.append("  for i = 1, #val, 1 do\n");
//        sb.append("    redis.call('sadd', 'delay:i', tostring(i))\n");
//        sb.append("    redis.call('sadd', 'delay:val:item', val[i])\n");
        //unpack()函数是接受一个数组来作为输入参数，并默认从下标为1开始返回所有元素。
        sb.append("    redis.call('rpush', KEYS[3], val[i])\n");
        sb.append("  end\n");
        sb.append("  return 1\n");
        sb.append("end\n");
        sb.append("return 0");
        LUA_SCRIPT = sb.toString();
    }

    /**
     * 5秒钟扫描一次等待队列
     */
    @Scheduled(cron = "${ss.distributeTask.scheduled:0/5 * * * * ?}")
    public void scheduledTask() {
        try {
            //waitTopics为String.format("delay:wait:%s", topic)
            Set<String> waitTopics = redisTemplate.opsForSet().members(DelayQueueConstants.META_TOPIC_WAIT);
            assert waitTopics != null;
            for (String waitTopic : waitTopics) {
                if (!redisTemplate.hasKey(waitTopic)) {
                    // 如果 KEY 不存在元数据中删除
                    redisTemplate.opsForSet().remove(DelayQueueConstants.META_TOPIC_WAIT, waitTopic);
                    continue;
                }
                String activeTopic = waitTopic.replace(DelayQueueConstants.DELAY_WAIT_KEY, DelayQueueConstants.DELAY_ACTIVE_KEY);
                Object[] keys = new Object[]{serialize(waitTopic), serialize(DelayQueueConstants.META_TOPIC_ACTIVE), serialize(activeTopic)};
                Object[] values = new Object[]{serialize(String.valueOf(System.currentTimeMillis())), serialize(activeTopic)};
                Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                    Object nativeConnection = connection.getNativeConnection();
                    if (nativeConnection instanceof RedisAsyncCommands) {
                        RedisAsyncCommands commands = (RedisAsyncCommands) nativeConnection;
                        return (Long) commands.getStatefulConnection().sync().eval(LUA_SCRIPT, ScriptOutputType.INTEGER, keys, values);
                    } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                        RedisAdvancedClusterAsyncCommands commands = (RedisAdvancedClusterAsyncCommands) nativeConnection;
                        return (Long) commands.getStatefulConnection().sync().eval(LUA_SCRIPT, ScriptOutputType.INTEGER, keys, values);
                    }
                    return 0L;
                });
                if(result != null && result > 0) {
                    logger.info("延迟队列[2]，消息到期进入执行队列({}): {}", activeTopic);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * serialize
     * @param key
     * @return
     */
    private byte[] serialize(String key) {
        RedisSerializer<String> stringRedisSerializer =
                (RedisSerializer<String>) redisTemplate.getKeySerializer();
        //lettuce连接包下序列化键值，否则无法用默认的ByteArrayCodec解析
        return stringRedisSerializer.serialize(key);
    }

}