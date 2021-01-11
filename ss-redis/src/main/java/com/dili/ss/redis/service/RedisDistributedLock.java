package com.dili.ss.redis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * redis分布式锁
 * @author asiamaster
 * @since 2019/2/1
 **/
@Component
@ConditionalOnExpression("'${redis.enable}'=='true'")
public class RedisDistributedLock{

    private final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    protected RedisTemplate redisTemplate;

    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    @Resource
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取锁，未获取或异常到返回false
     * key作为获取锁的条件，key和value将作为释放锁的条件
     * @param key
     * @param value
     * @param expire 单位秒
     * @return  获取失败返回false
     */
    public boolean tryGetLock(String key, String value,  long expire) {
        try {
            RedisCallback<Boolean> callback = (connection) -> {
                return connection.set(key.getBytes(Charset.forName("UTF-8")), value.getBytes(Charset.forName("UTF-8")), Expiration.seconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
            };
            return (Boolean)redisTemplate.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 同步获取锁，异常时返回false
     * key作为获取锁的条件，key和value将作为释放锁的条件
     * 锁无限等待，每次获取失败后睡眠10毫秒
     * @param key
     * @param value
     * @param expire 单位秒
     * @return  获取失败返回false
     */
    public boolean tryGetLockSync(String key, String value, long expire) {
        return tryGetLockSync(key, value, expire, Long.MAX_VALUE, TimeUnit.SECONDS, Long.MAX_VALUE, 10L, TimeUnit.MILLISECONDS);
    }



    /**
     * 同步获取锁，异常时返回false
     * key作为获取锁的条件，key和value将作为释放锁的条件
     * @param key
     * @param value
     * @param expire 锁过期时间, 单位秒
     * @param awaitTime  锁等待超时时间
     * @param awaitUnit  锁等待超时时间单位
     * @param retryCount 最大获取次数
     * @param sleepTime  每次获取失败后的睡眠时间
     * @param sleepUnit  每次获取失败后的睡眠时间单位
     * @return  获取失败返回false
     */
    public boolean tryGetLockSync(String key, String value, long expire, long awaitTime, TimeUnit awaitUnit, long retryCount, long sleepTime, TimeUnit sleepUnit) {
        if(tryGetLock(key, value, expire)){
            return true;
        }
        //不行再获取
        long nanos = awaitUnit.toNanos(awaitTime);
        final long deadline = System.nanoTime() + nanos;
        int count = 0;
        while (true) {
            nanos = deadline - System.nanoTime();
            //超时
            if (nanos <= 0L) {
                return false;
            }
            if (tryGetLock(key, value, expire)) {
                return true;
            }
            //如果大于最大获取次数或者线程被中断
            if (count++ > retryCount || Thread.interrupted()) {
                return false;
            }
            //阻塞
            LockSupport.parkNanos(sleepUnit.toNanos(sleepTime));
        }
    }


    /**
     * Lettuce方式释放锁
     * 根据lockKey从redis获取到的值和lockValue对比，如果相同则根据key删除缓存并返回true，不同则返回false
     * @param lockKey
     * @param lockValue
     * @return
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        RedisCallback<Boolean> callback = (connection) -> {
            return connection.eval(UNLOCK_LUA.getBytes(), ReturnType.BOOLEAN ,1, lockKey.getBytes(Charset.forName("UTF-8")), lockValue.getBytes(Charset.forName("UTF-8")));
        };
        return (Boolean)redisTemplate.execute(callback);
    }
}
