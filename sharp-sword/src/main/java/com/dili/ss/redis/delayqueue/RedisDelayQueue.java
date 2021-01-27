package com.dili.ss.redis.delayqueue;

/**
 * 延迟队列
 *
 * @author wm
 * @date 2021-01-26
 */
public interface RedisDelayQueue<E extends DelayMessage> {

    /**
     * 拉取消息
     */
    void poll();

    /**
     * 推送延迟消息
     *
     * @param e
     */
    void push(E e);
}
