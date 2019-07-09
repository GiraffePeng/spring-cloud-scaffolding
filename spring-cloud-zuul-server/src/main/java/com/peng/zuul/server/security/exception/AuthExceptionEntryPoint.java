package com.peng.zuul.server.security.exception;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * 无效token 或token不存在异常类重写
 *
 */
@Component
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws ServletException {
        Throwable cause = authException.getCause();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "401");
        jsonObject.put("data", "");
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            if(cause instanceof InvalidTokenException) {
            	jsonObject.put("msg", "token格式非法或失效");
                response.getWriter().write(jsonObject.toJSONString());
            }else{
            	jsonObject.put("msg", "token缺失");
                response.getWriter().write(jsonObject.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
