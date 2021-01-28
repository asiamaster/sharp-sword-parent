package com.dili.ss.component;

import com.dili.ss.java.B;
import com.dili.ss.util.ICustomThreadPoolExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 自定义线程池缓存,实现阻塞提交任务功能
 * 解决如果不设置队列长度会OOM，设置队列长度，会有任务得不到处理的问题
 * @author: WM
 * @time: 2021/1/27 16:26
 */
@Component
public class CustomThreadPoolExecutorCache {
    public static final String DEFAULT_KEY = "default";

    private Map<String, ExecutorService> executorServiceMap = new HashMap<>();

    /**
     * 初始化默认线程池
     */
    @PostConstruct
    public void init() {
        try {
            executorServiceMap.put(DEFAULT_KEY, ((Class<ICustomThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor());
        } catch (Exception e) {
        }
    }

    /**
     * 获取默认key的缓存线程池
     * @return
     */
    public ExecutorService getExecutor() {
        return executorServiceMap.get(DEFAULT_KEY);
    }

    /**
     * 获取指定key的缓存线程池
     * @return
     */
    public ExecutorService getExecutor(String key) {
        if(executorServiceMap.containsKey(key)) {
            return executorServiceMap.get(key);
        }
        synchronized(this){
            if(executorServiceMap.containsKey(key)) {
                return executorServiceMap.get(key);
            }
            try {
                executorServiceMap.put(key, ((Class<ICustomThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor());
            } catch (Exception e) {
            }
        }
        return executorServiceMap.get(key);
    }

    /**
     * 获取线程池缓存
     * @return
     */
    public Map<String, ExecutorService> getExecutorServiceMap() {
        return executorServiceMap;
    }
}
