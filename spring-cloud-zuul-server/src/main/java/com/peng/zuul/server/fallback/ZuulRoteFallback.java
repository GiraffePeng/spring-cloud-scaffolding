package com.peng.zuul.server.fallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * 定制服务级别的熔断处理
 */
@Component
public class ZuulRoteFallback implements FallbackProvider{

	/**
	 * 指定针对哪个服务进行熔断处理
	 */
	@Override
	public String getRoute() {
		return "userservice";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		return new ClientHttpResponse() {
			
			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
			}
			
			/**
			 * 熔断后返回的内容
			 */
			@Override
			public InputStream getBody() throws IOException {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("code", 500);
				jsonObject.put("msg", "用户服务不可用,请稍后重试");
				return new ByteArrayInputStream(jsonObject.toJSONString().getBytes());
			}
			
			@Override
			public String getStatusText() throws IOException {
				return this.getStatusCode().getReasonPhrase();
			}
			
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return HttpStatus.OK;
			}
			
			@Override
			public int getRawStatusCode() throws IOException {
				return this.getStatusCode().value();
			}
			
			@Override
			public void close() {
				
			}
		};
	}

}
