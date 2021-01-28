package com.dili.ss.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于导出的线程池，实现阻塞提交任务功能
 * 解决如果不设置队列长度会OOM，设置队列长度，会有任务得不到处理的问题
 * name:threadPoolExecutor
 * Created by asiamaster on 2017/9/11 0011.
 */
public class CustomThreadPoolExecutor implements ICustomThreadPoolExecutor {

	public CustomThreadPoolExecutor(){

	}
	/**
	 * 线程池初始化方法
	 *
	 * corePoolSize 核心线程池大小----1
	 * maximumPoolSize 最大线程池大小----3
	 * keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间----30+单位TimeUnit
	 * TimeUnit keepAliveTime时间单位----TimeUnit.MINUTES
	 * workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(5)====5容量的阻塞队列
	 * threadFactory 新建线程工厂----new CustomThreadFactory()====定制的线程工厂
	 * rejectedExecutionHandler 当提交任务数超过maxmumPoolSize+workQueue之和时,
	 *                          即当提交第41个任务时(前面线程都没有执行完,此测试方法中用sleep(100)),
	 *                                任务会交给RejectedExecutionHandler来处理
	 */
	@Override
	public ExecutorService getCustomThreadPoolExecutor() {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(),
				2 * Runtime.getRuntime().availableProcessors() + 1,
				20,
				TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(5),
				new CustomThreadFactory(),
				new CustomRejectedExecutionHandler());
		pool.allowCoreThreadTimeOut(true);
		return pool;
	}

	@Override
	public ExecutorService getCustomThreadPoolExecutor(int corePoolSize,
													   int maximumPoolSize,
													   long keepAliveTime,
													   int workQueueSize) {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTime,
				TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(workQueueSize),
				new CustomThreadFactory(),
				new CustomRejectedExecutionHandler());
		pool.allowCoreThreadTimeOut(true);
		return pool;
	}

//	public void destory() {
//		if(pool != null) {
//			pool.shutdownNow();
//		}
//	}

	private class CustomThreadFactory implements ThreadFactory {
		private AtomicInteger count = new AtomicInteger(0);
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			String threadName = CustomThreadPoolExecutor.class.getSimpleName() + count.addAndGet(1);
//			System.out.println("create thread:"+threadName);
			t.setName(threadName);
			return t;
		}
	}

	private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			try {
				// 核心改造点，由blockingqueue的offer改成put阻塞方法
//				System.out.println("reject:"+executor.getClass().getName());
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// 测试构造的线程池
//	public static void main(String[] args) {
//		ExportThreadPoolExecutor exec = new ExportThreadPoolExecutor();
//		// 1.初始化
//		exec.init();
//
//		ExecutorService pool = exec.getCustomThreadPoolExecutor();
//		for(int i=1; i<35; i++) {
//			System.out.println("提交第" + i + "个任务!");
//			pool.execute(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						System.out.println(">>>task is running=====");
//						TimeUnit.SECONDS.sleep(5);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//		pool.shutdown();
//		//判断是否所有的线程已经运行完
//		while (!pool.isTerminated()) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("还没完");
//		}
//		System.out.println("All is finished!");
//
//		// 2.销毁----此处不能销毁,因为任务没有提交执行完,如果销毁线程池,任务也就无法执行了
//		// exec.destory();
//
//	}
}