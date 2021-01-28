package com.dili.ss.redis.delayqueue;

import com.dili.ss.redis.delayqueue.dto.DelayMessage;

/**
 * 延时队列接口
 *
 * @author wm
 * @date 2021-01-26
 */
public interface RedisDelayQueue<E extends DelayMessage> {

    /**
     * 拉取消息，暂未实现
     */
    void poll();

    /**
     * 推送延迟消息
     *
     * @param e
     */
    void push(E e);
}
