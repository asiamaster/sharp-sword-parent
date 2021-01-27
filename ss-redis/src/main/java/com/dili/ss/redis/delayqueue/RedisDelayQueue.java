package com.dili.ss.redis.delayqueue;

/**
 * 延迟队列
 *
 * @author wm
 * @date 2021-01-26
 */
public interface RedisDelayQueue<E extends DelayMessage> {
    //待处理列表的topic列表key，加入后就不会删除，用于作为固定的key获取待处理的topic队列
    String META_TOPIC_WAIT = "delay:meta:topic:wait";
    //待激活列表的topic列表key，加入后就不会删除，用于作为固定的key获取待激活的topic队列
    String META_TOPIC_ACTIVE = "delay:meta:topic:active";
    //单实例版消息处理器只需要一个topic key列表
    String META_TOPIC = "delay:meta:topic";

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
