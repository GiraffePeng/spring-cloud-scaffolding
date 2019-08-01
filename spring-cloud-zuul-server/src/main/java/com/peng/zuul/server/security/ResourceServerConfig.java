package com.peng.zuul.server.security;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.FileCopyUtils;

import com.peng.zuul.server.security.exception.AuthExceptionEntryPoint;
import com.peng.zuul.server.security.exception.CustomAccessDeniedHandler;

@Configuration
@EnableResourceServer//@EnableResourceServer启用资源服务器
public class ResourceServerConfig extends ResourceServerConfigurerAdapter{
	
	@Autowired
	private CustomAccessDeniedHandler customAccessDeniedHandler;
	
	@Autowired
	private AuthExceptionEntryPoint authExceptionEntryPoint;
	
	@Autowired
	private FilterIgnorePropertiesConfig filterIgnorePropertiesConfig;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry hasAuthority = http.authorizeRequests();
        //从yml配置文件中获取不需要认证的url(即匿名能访问的url)
        filterIgnorePropertiesConfig.getUrls().forEach(url -> hasAuthority.antMatchers(url).permitAll());
        hasAuthority.antMatchers("/**").hasAuthority("USER");//除上述url，其他url必须携带权限有USER的token才可以访问
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore());//声明了资源服务器的TokenStore是JWT以及公钥
        
        resources.authenticationEntryPoint(authExceptionEntryPoint)//无效token 或token不存在异常类重写
                .accessDeniedHandler(customAccessDeniedHandler);//权限不足异常类重写
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
    	// 用作JWT转换器  获取resource目录下的public.cert文件
        JwtAccessTokenConverter converter =  new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource("public.cert");
        String publicKey;
        try {
            publicKey = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //设置公钥
        converter.setVerifierKey(publicKey);
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
}
