package me.josephzhu.springcloud101.auth.server;


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

/**
 * 由于 auth-service 需要对外暴露检查 Token 的 API 接口，所以 auth-service 
 * 其实也是一个 资源服务，
 * 需要在 auth-service 中引入 Spring Security，并完成相关配置，从而对 auth-service 的 资源 进行保护。
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
    private UserServiceDetail userServiceDetail;
	
	 @Override
	 protected void configure(HttpSecurity http) throws Exception {
		 http.csrf().disable() //关闭CSRF
         .authorizeRequests()
         .anyRequest()
         .authenticated()
     .and()
         .httpBasic();
		/* 
		 http.authorizeRequests().anyRequest().permitAll()
         .and()
         .csrf().disable()*/
	 }
 
	 @Bean
	 public BCryptPasswordEncoder passwordEncoder(){
		 return new BCryptPasswordEncoder();
	 }

	 @Override
	 protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		 auth.userDetailsService(userServiceDetail).passwordEncoder(passwordEncoder());
	 }
	 
	 @Override
	 public void configure(WebSecurity web) throws Exception {
		 web.ignoring().antMatchers("/favor.ioc");
	 }

	 @Override
	 public @Bean AuthenticationManager authenticationManagerBean() throws Exception {
	     return super.authenticationManagerBean();
	 }
	
}
