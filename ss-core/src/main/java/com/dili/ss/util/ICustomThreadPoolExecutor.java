package com.dili.ss.util;

import java.util.concurrent.ExecutorService;

public interface ICustomThreadPoolExecutor {

	/**
	 * 创建默认线程池
	 * @return
	 */
	ExecutorService getCustomThreadPoolExecutor();

	/**
	 * 创建线程池
	 * @param corePoolSize 核心线程池大小
	 * @param maximumPoolSize 最大线程池大小
	 * @param keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间，单位秒
	 * @param workQueueSize 阻塞队列容量
	 * @return
	 */
	ExecutorService getCustomThreadPoolExecutor(int corePoolSize,
												int maximumPoolSize,
												long keepAliveTime,
												int workQueueSize);
}