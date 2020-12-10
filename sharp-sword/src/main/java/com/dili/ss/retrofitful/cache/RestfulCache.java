package com.dili.ss.retrofitful.cache;

import java.util.Map;

/**
 * retrofitful的上下文缓存
 * @author: WM
 * @time: 2020/12/9 17:17
 */
public class RestfulCache {
    /**
     * retrofitful的header缓存，用于不使用@ReqHeader传参
     */
    public static final ThreadLocal<Map<String, String>> RESTFUL_HEADER_THREAD_LOCAL = new ThreadLocal<>();
}
