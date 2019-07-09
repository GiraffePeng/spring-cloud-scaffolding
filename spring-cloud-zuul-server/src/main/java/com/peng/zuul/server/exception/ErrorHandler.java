package com.peng.zuul.server.exception;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * zuul网关层统一的异常处理，错误返回格式的修改
 */
@RestController
public class ErrorHandler implements ErrorController{
	
	private final ErrorAttributes errorAttributes;
	 
    @Autowired
    public ErrorHandler(ErrorAttributes errorAttributes) {
    	this.errorAttributes = errorAttributes;
    }

	@RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ErrorResult error(HttpServletRequest request) {
		WebRequest webRequest = new ServletWebRequest(request);
		Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(webRequest, true);
		String msg = errorAttributes.getOrDefault("error", "not found").toString();
		String code = errorAttributes.getOrDefault("status", 404).toString();
		return ErrorResult.builder().code(Integer.valueOf(code)).msg(msg).build();
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
