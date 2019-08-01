package com.peng.auth.server.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.peng.auth.server.repository.UserServiceDetail;

/**
 * 由于 auth-service 需要对外暴露检查 Token 的 API 接口，所以 auth-service 
 * 其实也是一个 资源服务，
 * 需要在 auth-service 中引入 Spring Security，并完成相关配置，从而对 auth-service 的 资源 进行保护。
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
    private UserServiceDetail userServiceDetail;
	
	 @Override
	 protected void configure(HttpSecurity http) throws Exception {
		 http.csrf().disable() //关闭CSRF
         .authorizeRequests()
         .antMatchers("/oauth/authorize")//开放/oauth/authorize路径的匿名访问，后者用于换授权码，这个端点访问的时候在登录之前
         .permitAll()
         .anyRequest() //其他所有路径访问，进行验证
         .authenticated()
     .and()
         .httpBasic();
	 }
 
	 @Bean
	 public BCryptPasswordEncoder passwordEncoder(){
		 return new BCryptPasswordEncoder();
	 }

	 //我们把用户存在了数据库中希望配置JDBC的方式，此外，我们还配置了使用BCryptPasswordEncoder加密来保存用户的密码（生产环境的用户密码肯定不能是明文保存）
	 @Override
	 protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		 auth.userDetailsService(userServiceDetail).passwordEncoder(passwordEncoder());
	 }
	 
	 @Override
	 public @Bean AuthenticationManager authenticationManagerBean() throws Exception {
	     return super.authenticationManagerBean();
	 }
	
}
