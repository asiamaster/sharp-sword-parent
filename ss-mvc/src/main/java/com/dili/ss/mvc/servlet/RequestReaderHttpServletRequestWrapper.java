package com.dili.ss.mvc.servlet;

import com.dili.ss.exception.AppException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;

/**
 * 解决ServletInputStream不支持重复读
 * Java HttpServeletRequest的ServletInputStream的markSupported()方法返回false，所以stream不支持reset方法，不支持重复读。
 * 如果interceptor中读取了Stream内容，在controller这一层，就获取不到参数和请求内容。所以，要解决stream重复读。
 */
public class RequestReaderHttpServletRequestWrapper extends ContentCachingRequestWrapper {

    private final byte[] body;

    /**
     * 将body取出存储起来然后再放回去，但是在request.getParameter()时数据就会丢失
     * 调用getParameterMap()，目的将参数Map从body中取出，这样后续的任何request.getParamter（）都会有值
     * @param request request
     * @throws IOException io异常
     */
    public RequestReaderHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        request.getParameterMap();//此处将body中的parameter取出来，，这样后续的任何request.getParamter（）都会有值
        this.body = getBodyString(request).getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }


    /**
     * 获取请求Body
     * @param request 过滤后被的request
     * @return 返回body
     */
    private String getBodyString(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            throw new AppException("获取requestBody出错：" + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
            }
        }
        return sb.toString();
    }
}