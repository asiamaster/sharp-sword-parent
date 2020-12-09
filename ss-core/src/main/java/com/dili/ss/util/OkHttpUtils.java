package com.dili.ss.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OKHttp工具类
 * @author: WangMi
 * @time: 2020/12/7 16:38
 */
public class OkHttpUtils {

    public final static Logger log = LoggerFactory.getLogger(OkHttpUtils.class);
    private static OkHttpClient okHttpClient = null;
    private static final String EX_STRING_FORMAT = "远程调用失败, URL:[%s],参数:[%s],code:[%s],消息:[%s]";

    static{
        //初始化okHttpClient
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.HOURS)
                .build();
    }

    /**
     * 获取okHttpClient，用于复制后自定义属性
     * OkHttpClient client1 = client.newBuilder()
     *         .readTimeout(500, TimeUnit.MILLISECONDS)
     *         .build();
     * @return
     */
    public static OkHttpClient getOkHttpClient(){
        return okHttpClient;
    }

    /**
     * GET, 异步
     * @param url
     * @param paramsMap
     * @param headersMap
     * @param tag
     * @return
     * @throws Exception
     */
    public static void getAsync(String url, Map<String, String> paramsMap, Map<String, String> headersMap, Object tag, Callback callback) {
        Request request = new Request.Builder()
                .url(appendParams(url, paramsMap))
                .get()
                .tag(tag)
                .headers(buildHeaders(headersMap))
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * GET
     * @param url
     * @param paramsMap
     * @param headersMap
     * @param tag
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, String> paramsMap, Map<String, String> headersMap, Object tag) throws IOException {
        Request request = new Request.Builder()
                .url(appendParams(url, paramsMap))
                .get()
                .tag(tag)
                .headers(buildHeaders(headersMap))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(String.format(EX_STRING_FORMAT, url, paramsMap, response.code(), response.message()));
            }
            return response.body().string();
        }
    }

    /**
     * POST 表单参数
     * @param url
     * @param paramsMap
     * @param headersMap
     * @param tag
     * @return
     * @throws IOException
     */
    public static String postFormParameters(String url, Map<String, String> paramsMap, Map<String, String> headersMap, Object tag) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .tag(tag)
                .post(buildFormParams(paramsMap))
                .headers(buildHeaders(headersMap))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(String.format(EX_STRING_FORMAT, url, paramsMap, response.code(), response.message()));
            }
            return response.body().string();
        }
    }

    /**
     * POST 表单参数，异步
     * @param url
     * @param paramsMap
     * @param headersMap
     * @param tag
     * @return
     * @throws Exception
     */
    public static void postFormParametersAsync(String url, Map<String, String> paramsMap, Map<String, String> headersMap, Object tag, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .tag(tag)
                .post(buildFormParams(paramsMap))
                .headers(buildHeaders(headersMap))
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * POST Body String参数
     * @param url
     * @param postBody
     * @param headersMap
     * @param tag
     * @return
     * @throws IOException
     */
    public static String postBodyString(String url, String postBody, Map<String, String> headersMap, Object tag) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .tag(tag)
                .headers(buildHeaders(headersMap))
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBody))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(String.format(EX_STRING_FORMAT, url, postBody, response.code(), response.message()));
            }
            return response.body().string();
        }
    }

    /**
     * POST Body String参数，异步
     * @param url
     * @param postBody
     * @param headersMap
     * @param tag
     * @return
     */
    public static void postBodyStringAsync(String url, String postBody, Map<String, String> headersMap, Object tag, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .tag(tag)
                .headers(buildHeaders(headersMap))
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBody))
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 根据Tag取消请求
     * @param tag
     */
    public static void cancelTag(Object tag) {
        if (tag == null) {
            return;
        }
        for (Call call : okHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : okHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 根据Tag取消请求
     * @param client
     * @param tag
     */
    public static void cancelTag(OkHttpClient client, Object tag) {
        if (client == null || tag == null) {
            return;
        }
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 取消所有请求请求
     */
    public static void cancelAll() {
        for (Call call : okHttpClient.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : okHttpClient.dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    /**
     * 取消所有请求请求
     * @param client
     */
    public static void cancelAll(OkHttpClient client) {
        if (client == null) {
            return;
        }
        for (Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }
    }



    // ===============================   私有方法分割线    ===============================

    /**
     * 构建表单参数
     * @param paramsMap
     * @return
     */
    protected static FormBody buildFormParams(Map<String, String> paramsMap) {
        FormBody.Builder builder = new FormBody.Builder();
        if (paramsMap != null) {
            Iterator<String> iterator = paramsMap.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next();
                String value = paramsMap.get(key);
                if(value == null){
                    continue;
                }
                builder.add(key, value);
            }
        }
        return builder.build();
    }

    /**
     * 构建headers
     * @param headersParams
     * @return
     */
    protected static Headers buildHeaders(Map<String, String> headersParams) {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (headersParams != null) {
            Iterator<String> iterator = headersParams.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next();
                String value = headersParams.get(key);
                if(value == null){
                    continue;
                }
                headersBuilder.add(key, value);
            }
        }
        return headersBuilder.build();
    }

    /**
     * 附加URL参数
     * @param url
     * @param params
     * @return
     */
    protected static String appendParams(String url, Map<String, String> params){
        if (url == null || params == null || params.isEmpty()){
            return url;
        }
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                appendQueryString(key, value, query);
            }
        }
        if (query.length() > 0) {
            query.replace(0, 1, "?");
        }

        return url + query.toString();
    }

    /**
     * 添加单个URL参数
     * @param key
     * @param v
     * @param sb
     */
    protected static void appendQueryString(String key, Object v, StringBuilder sb) {
        if (v == null) {
            return;
        }
        String value = String.valueOf(v);
        if (value.trim().length() == 0) {
            return;
        }
        sb.append("&").append(key).append("=").append(encodeUrl(value));
    }

    /**
     * URL编码
     * @param value
     * @return
     */
    protected static String encodeUrl(String value) {
        String result;
        try {
            result = URLEncoder.encode(value, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            result = value;
        }
        return result;
    }

//    public static void main(String[] args) throws Exception {
//        String getUrl = "http://bpmc.diligrp.com:8617/api/runtime/listHistoricProcessInstance?processInstanceId=202012071449520760000000";
//        OkHttpUtils.postFormParametersAsync(getUrl, null, null,  "test", new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                System.out.println("failure...");
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                try (ResponseBody responseBody = response.body()) {
//                    if (!response.isSuccessful()) {
//                        throw new IOException("Unexpected code " + response);
//                    }
//                    Headers responseHeaders = response.headers();
//                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }
//                    System.out.println(responseBody.string());
//                }
//            }
//        });
//        Thread.sleep(1000L);
//        OkHttpUtils.cancelTag("test");
//        System.out.println("over");
//    }

}
