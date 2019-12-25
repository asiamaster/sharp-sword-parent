package com.dili.http.okhttp.builder;

import com.dili.http.okhttp.request.PostFileRequest;
import com.dili.http.okhttp.request.RequestCall;
import okhttp3.MediaType;

import java.io.File;

/**
 * Created by wm on 17/3/9.
 */
public class PostFileBuilder extends OkHttpRequestBuilder<PostFileBuilder>
{
    private File file;
    private MediaType mediaType;


    public OkHttpRequestBuilder file(File file)
    {
        this.file = file;
        return this;
    }

    public OkHttpRequestBuilder mediaType(MediaType mediaType)
    {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build()
    {
        return new PostFileRequest(url, tag, params, headers, file, mediaType,id).build();
    }


}
