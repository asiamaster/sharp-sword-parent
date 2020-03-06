package com.dili.ss.mvc.servlet;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
//@WebFilter(filterName="ssFilter",urlPatterns="/*")
public class SSFilter implements Filter {

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        //获取请求中的流，将取出来的字符串，再次转换成流，然后把它放入到新request对象中。
        //request应该是RequestFacade
        if (request instanceof HttpServletRequest) {
            requestWrapper = new RequestReaderHttpServletRequestWrapper((HttpServletRequest) request);
        }

        // 在chain.doFiler方法中传递新的request对象
        if (requestWrapper == null) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void destroy() {

    }

}