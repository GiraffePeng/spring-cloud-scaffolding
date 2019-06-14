package me.josephzhu.springcloud101.auth.server;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringCloudApplication
@EnableHystrix
@Configuration
public class AuthServerApplicaiton {

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplicaiton.class, args);
	}
}
