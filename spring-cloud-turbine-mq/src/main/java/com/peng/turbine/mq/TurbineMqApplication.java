package com.peng.turbine.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.turbine.stream.EnableTurbineStream;
//开启turbine通过mq方式获取信息的方式
@EnableTurbineStream
@SpringBootApplication
@EnableDiscoveryClient
public class TurbineMqApplication {
	
	public static void main(String[] args) {
        SpringApplication.run(TurbineMqApplication.class, args);
    }
}
