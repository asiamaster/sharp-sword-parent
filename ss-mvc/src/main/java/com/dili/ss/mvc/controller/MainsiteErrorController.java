package com.dili.ss.mvc.controller;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by asiamaster on 2017/11/30 0030.
 */
@Controller("error")//参考ErrorMvcAutoConfiguration
@RequestMapping("/error")
public class MainsiteErrorController implements ErrorController {

	protected static final Logger log = LoggerFactory.getLogger(MainsiteErrorController.class);
	private static final String ERROR_PATH = "/error";
	@Autowired
	private ErrorAttributes errorAttributes;

	/**
	 * ajax请求错误<br/>
	 * 前端ajax获取方式:
	 * error: function(XMLHttpRequest, textStatus, errorThrown){
	 * 		alert("responseText:"+XMLHttpRequest.responseText);
	 * }
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public BaseOutput<String> ajaxError(WebRequest request, HttpServletResponse response){
//		Map<String, Object> body = getErrorAttributes(request, true);
//		HttpStatus status = getStatus(request);
		BaseOutput baseOutput = buildBody(request,true);
		log.error(JSONObject.toJSONString(baseOutput));
		return baseOutput;
	}

	/**
	 * 页面错误
	 * 在springboot项目中当我们访问一个不存在的url时调用
	 * 但是其获取不到异常的具体信息，也无法根据异常类型进行不同的响应
	 * 所有正常情况下是到@ControllerAdvice设置的异常拦截中
	 * @ControllerAdvice报错或者页面找不到，也会进入这里
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(produces = "text/html")
	public String handleError(HttpServletRequest request, HttpServletResponse response){
		//获取statusCode:401,404,500
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if(statusCode == 401){
			return SpringUtil.getProperty("error.page.404", "error/noAccess");
		}
//		else if(statusCode == 404){
//			return "/404"
//		}else if(statusCode == 403){
//			return "/403"
//		}else{
//			return "/500"
//		}
		return SpringUtil.getProperty("error.page.404", "error/404");
	}

	/**
	 * 未登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/noLogin.do")
	public String noLogin(HttpServletRequest request, HttpServletResponse response){
		return SpringUtil.getProperty("error.page.noLogin", "error/noLogin");
	}

	/**
	 * 页面没有权限
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/noAccess.do")
	public String noAccess(HttpServletRequest request, HttpServletResponse response){
		return SpringUtil.getProperty("error.page.noAccess", "error/noAccess");
	}

	/**
	 * 错误页面路径
	 * @return
	 */
	@Override
	public String getErrorPath() {
		return ERROR_PATH+"/default";
	}

	/**
	 * 获取错误属性
	 * @param request
	 * @param includeStackTrace
	 * @return
	 */
	private Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
//		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		return errorAttributes.getErrorAttributes(request, includeStackTrace);
	}

	/**
	 * 构建ajax请求错误的http body
	 * @param request
	 * @param includeStackTrace
	 * @return
	 */
	private BaseOutput buildBody(WebRequest request,Boolean includeStackTrace){
		Map<String,Object> errorAttributes = getErrorAttributes(request, includeStackTrace);
		Integer status=(Integer)errorAttributes.get("status");
		String path=(String)errorAttributes.get("path");
		String messageFound=(String)errorAttributes.get("message");
		String message="";
		String trace ="";
		if(!StringUtils.isEmpty(path)){
			message=String.format("Requested path %s with result %s",path,messageFound);
		}
		if(includeStackTrace) {
			trace = (String) errorAttributes.get("trace");
			if(!StringUtils.isEmpty(trace)) {
				message += String.format(" and trace %s", trace);
			}
		}
		return BaseOutput.failure(message).setCode(status.toString());
	}
}
