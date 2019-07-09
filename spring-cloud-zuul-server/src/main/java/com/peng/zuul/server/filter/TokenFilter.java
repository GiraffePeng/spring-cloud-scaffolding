package com.peng.zuul.server.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;


import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
public class TokenFilter extends ZuulFilter {
	
	/**
	 * 四种类型：pre,routing,error,post
    pre：主要用在路由映射的阶段是寻找路由映射表的
    routing:具体的路由转发过滤器是在routing路由器，具体的请求转发的时候会调用
    error:一旦前面的过滤器出错了，会调用error过滤器。
    post:当routing，error运行完后才会调用该过滤器，是在最后阶段的
	 */
    @Override
    public String filterType() {
        return PRE_TYPE;
    }
    
    //自定义过滤器执行的顺序，数值越大越靠后执行，越小就越先执行
    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    //控制过滤器生效不生效，可以在里面写一串逻辑来控制
    @Override
    public boolean shouldFilter() {
        return true;
    }
    
    //执行过滤逻辑
    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        
        HttpServletRequest request = ctx.getRequest();
        String token = "2112"; //request.getParameter("token");
        if(token == null) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().setCharacterEncoding("UTF-8");
                ctx.getResponse().getWriter().write("禁止访问");
            } catch (Exception e){}

            return null;
        }
        return null;
    }
}
