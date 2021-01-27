package com.dili.ss.component;

import com.dili.ss.java.B;
import com.dili.ss.util.IExportThreadPoolExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

/**
 * 自定义线程池,实现阻塞提交任务功能
 * 解决如果不设置队列长度会OOM，设置队列长度，会有任务得不到处理的问题
 * @author: WM
 * @time: 2021/1/27 16:26
 */
@Component
public class CustomThreadPoolExecutor {
    //    多线程执行器
    private ExecutorService executor;

    @PostConstruct
    public void init() {
        try {
            executor = ((Class<IExportThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor();
        } catch (Exception e) {
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
