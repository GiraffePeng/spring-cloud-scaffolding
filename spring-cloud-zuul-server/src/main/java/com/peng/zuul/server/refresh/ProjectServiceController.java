package com.peng.zuul.server.refresh;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.peng.zuul.server.config.ZuulRoute;


import java.util.List;

@RestController
public class ProjectServiceController {

    @Autowired
    RedisTemplate redisTemplate;
    
    private final static String ZUUL_ROUTE = "_ROUTE_KEY";

    /**
     * 通过传递参数 
     * path：路由路径
     * serviceId：服务名称
     * stripPrefix：转发去掉前缀
     * enabled：是否启用
     * retryable：是否重试
     * 来动态变更路由消息。
     * @param zuulRoutes
     */
    @PostMapping("/zuulRoute/refresh")
    public void refreshZuulRoute(@RequestBody List<ZuulRoute> zuulRoutes){
    	for (ZuulRoute zuulRoute : zuulRoutes) {
    		if(StringUtils.isBlank(zuulRoute.getPath()) || StringUtils.isBlank(zuulRoute.getServiceId())) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
        	if(StringUtils.isBlank(zuulRoute.getStripPrefix()) || !(zuulRoute.getStripPrefix().equals("1") || zuulRoute.getStripPrefix().equals("0"))) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
        	if(StringUtils.isBlank(zuulRoute.getEnabled()) || !(zuulRoute.getEnabled().equals("1") || zuulRoute.getEnabled().equals("0"))) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
        	if(StringUtils.isBlank(zuulRoute.getRetryable()) || !(zuulRoute.getRetryable().equals("1") || zuulRoute.getRetryable().equals("0"))) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
		}
    	ValueOperations opsForValue = redisTemplate.opsForValue();
    	opsForValue.set(ZUUL_ROUTE, JSONObject.toJSONString(zuulRoutes));
    }

}
