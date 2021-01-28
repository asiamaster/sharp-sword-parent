package com.dili.ss.redis.delayqueue.consts;

/**
 * 延时队列使用到的常量
 * @description:
 * @author: WM
 * @time: 2021/1/27 16:59
 */
public interface DelayQueueConstants {
    //待处理列表的topic列表key，加入后就不会删除，用于作为固定的key获取待处理的topic队列
    String META_TOPIC_WAIT = "delay:meta:topic:wait";
    //待激活列表的topic列表key，加入后就不会删除，用于作为固定的key获取待激活的topic队列
    String META_TOPIC_ACTIVE = "delay:meta:topic:active";
    //单实例版消息处理器只需要一个topic key列表
    String META_TOPIC = "delay:meta:topic";

    /**
     * 分布式延时等待队列
     */
    String DELAY_WAIT_KEY = "delay:wait:";
    /**
     * 分布式延时激活队列
     */
    String DELAY_ACTIVE_KEY = "delay:active:";
    /**
     * 分布式延时失败队列
     */
    String DELAY_FAIL_KEY = "delay:fail:";

    /**
     * 单机延时队列
     */
    String DELAY_QUEUE_KEY = "delay:queue:";

    /**
     * 单机延时失败队列
     */
    String DELAY_QUEUE_FAIL_KEY = "delay:queue:fail:";

    /**
     * 延时队列线程池
     */
    String DELAY_QUEUE_EXECUTOR_KEY = "delay_queue_executor";
}
