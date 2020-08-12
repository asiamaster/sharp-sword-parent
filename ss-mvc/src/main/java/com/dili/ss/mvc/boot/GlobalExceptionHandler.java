package com.dili.ss.mvc.boot;

import com.alibaba.fastjson.JSON;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.SpringUtil;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一异常处理
 */
@ControllerAdvice
@ConditionalOnExpression("'${globalExceptionHandler.enable}'=='true'")
public class GlobalExceptionHandler {

//    /**
//     * 未登录异常处理
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(NotLoginException.class)
//    public String defultExcepitonHandler(HttpServletRequest request, HttpServletResponse response, NotLoginException e) throws IOException {
//        e.printStackTrace();
//        String requestType = request.getHeader("X-Requested-With");
//        if (requestType == null) {
//            request.setAttribute("exception", e);
//            return SpringUtil.getProperty("error.page.noLogin", "error/noLogin");
//        }
//        response.setContentType("application/json;charset=UTF-8");
//        return JSON.toJSONString(BaseOutput.failure(e.getMessage()));
//    }
//
//    /**
//     * 无访问权限异常处理
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(NotAccessPermissionException.class)
//    public String defultExcepitonHandler(HttpServletRequest request, HttpServletResponse response, NotAccessPermissionException e) throws IOException {
//        e.printStackTrace();
//        String requestType = request.getHeader("X-Requested-With");
//        if (requestType == null) {
//            request.setAttribute("exception", e);
//            return SpringUtil.getProperty("error.page.noAccess", "error/noAccess");
//        }
//        response.setContentType("application/json;charset=UTF-8");
//        return JSON.toJSONString(BaseOutput.failure(e.getMessage()));
//    }

    @ExceptionHandler(InternalException.class)
    public String internalExceptionHandler(HttpServletRequest request, HttpServletResponse response, InternalException e) throws IOException {
        e.printStackTrace();
        //判断请求类型是json
        if(request.getHeader("content-type").equals("application/json")){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(BaseOutput.failure(e.getMessage())));
            return null;
        }
        //判断是否是ajax访问，不是则跳到默认错误页面
        if (request.getHeader("X-Requested-With") == null) {
            request.setAttribute("exception", e);
            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
        return JSON.toJSONString(BaseOutput.failure(e.getMessage()));
    }

    /**
     * 全局异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public String defultExcepitonHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        Exception exception = e;
        e.printStackTrace();
        String exMsg = exception.getMessage();
        //Sentinel限流引发的异常
        if(e.getCause() != null && "com.alibaba.csp.sentinel.slots.block.flow.FlowException".equals(e.getCause().toString())){
            exception = (Exception)e.getCause();
            exMsg = "服务开启限流保护,请稍后再试!";
        }
        //判断请求类型是json
        if(request.getHeader("content-type").equals("application/json")){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(BaseOutput.failure(exMsg)));
            return null;
        }
        //判断是否是ajax访问，不是则跳到默认错误页面
        if (request.getHeader("X-Requested-With") == null) {
            request.setAttribute("exception", exception);
            request.setAttribute("exMsg", exMsg);
//            response.sendRedirect(basePath + SpringUtil.getProperty("error.page.default", "error/default"));
            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(BaseOutput.failure(exMsg)));
        return null;
    }

}