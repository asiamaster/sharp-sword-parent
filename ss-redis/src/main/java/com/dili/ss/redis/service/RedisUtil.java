package com.dili.ss.redis.service;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 工具类
 *
 */
@SuppressWarnings("unchecked")
@Component
@ConditionalOnExpression("'${redis.enable}'=='true'")
public class RedisUtil {
    @SuppressWarnings("rawtypes")
    @Resource(name="redisTemplate")
    protected RedisTemplate redisTemplate;

    /**
     * 集合、列表、Set等对象自行取redisTemplate操作
     * @return
     */
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }
    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
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
    public Boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }
    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public Long lPush(String k, Object v) {
        return redisTemplate.opsForList().rightPush(k, v);
    }

    /**
     * 列表获取
     *
     * @param k
     * @param start
     * @param end
     * @return
     */
    public List<Object> lRange(String k, long start, long end) {
        return redisTemplate.opsForList().range(k, start, end);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public <T> T get(final String key, Class<T> clazz) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        if(null != result && result.getClass().isAssignableFrom(clazz)){
            return (T)result;
        }
        return result == null ? null : JSON.parseObject(result.toString(), clazz);
    }

    /**
     * 根据key自增value
     * @param key
     * @param value
     * @return
     */
    public Long increment(String key, Long value){
        return redisTemplate.opsForValue().increment(key, value);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public void set(final String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */
    public Long add(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */
    public Boolean zAdd(String key, Object value, double scoure) {
        return redisTemplate.opsForZSet().add(key, value, scoure);
    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<Object> rangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 过期时间，单位秒
     * @return
     */
    public void set(final String key, Object value, Long expireTime) {
        set(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit 过期时间单位枚举
     * @return
     */
    public void set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return 是否获取成功
     */
    public Boolean setIfAbsent(final String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 过期时间，单位秒
     * @return 是否获取成功
     */
    public boolean setIfAbsent(final String key, Object value, Long expireTime) {
        return setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit 过期时间单位枚举
     * @return 是否获取成功
     */
    public Boolean setIfAbsent(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, timeUnit);
    }

    /**
     * 推后过期时间
     * @param key
     * @param timeout
     * @param timeUnit
     * @return
     */
    public Boolean expire(String key, long timeout, TimeUnit timeUnit){
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 推后过期时间到指定日期
     * @param key
     * @param date
     * @return
     */
    public Boolean expireAt(String key, Date date){
        return redisTemplate.expireAt(key, date);
    }
}
