package com.dili.http.okhttp.builder;

import com.dili.http.okhttp.request.GetRequest;
import com.dili.http.okhttp.request.RequestCall;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wm on 17/3/9.
 */
public class GetBuilder extends OkHttpRequestBuilder<GetBuilder> implements HasParamsable
{
    @Override
    public RequestCall build()
    {
        if (params != null)
        {
            url = appendParams(url, params);
        }

        return new GetRequest(url, tag, params, headers,id).build();
    }

    protected String appendParams(String url, Map<String, String> params)
    {
        if (url == null || params == null || params.isEmpty())
        {
            return url;
        }
//        Uri.Builder builder = Uri.parse(url).buildUpon();
//        Set<String> keys = params.keySet();
//        Iterator<String> iterator = keys.iterator();
//        while (iterator.hasNext())
//        {
//            String key = iterator.next();
//            builder.appendQueryParameter(key, params.get(key));
//        }
//        return builder.build().toString();
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

    void appendQueryString(String key, Object v, StringBuilder sb) {
        if (v == null) {
            return;
        }
        String value = String.valueOf(v);
        if (value.trim().length() == 0) {
            return;
        }
        sb.append("&").append(key).append("=").append(encodeUrl(value));
    }

    String encodeUrl(String value) {
        String result;
        try {
            result = URLEncoder.encode(value, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            result = value;
        }
        return result;
    }


    @Override
    public GetBuilder params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    @Override
    public GetBuilder addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }


}
