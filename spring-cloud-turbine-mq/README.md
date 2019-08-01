## 通过消息代理收集聚合
Spring Cloud 在封装 Turbine 的时候，还实现了基于消息代理的收集实现。所以，我们可以将所有需要收集的监控信息都输出到消息代理中，然后 Turbine 服务再从消息代理中异步的获取这些监控信息，最后将这些监控信息聚合并输出到 Hystrix Dashboard 中。通过引入消息代理，我们的 Turbine 和 Hystrix Dashoard 实现的监控架构可以改成如下图所示的结构：
### 架构图
![](https://user-gold-cdn.xitu.io/2019/8/1/16c4b3448c710c20?w=902&h=346&f=png&s=81206)

### 改造被监控的client端
以改造spring-cloud-userservice-server为例
#### 添加依赖
在被监控端需要增加hystrix、hystrix－stream和springcloud-stream的rabbitmq依赖，增加了这几个依赖就可以保证服务启动后会作为生产者向rabbitmq的queue中发送监控消息。
```
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    </dependency>
```
#### application.yml配置rabbitmq
```
server.port=8761
spring.application.name=userservice
spring.datasource.url=jdbc:mysql://localhost:3306/ceshi
spring.datasource.username=huaxin
spring.datasource.password=Koreyoshih527
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.zipkin.base-url=http://localhost:9411
spring.sleuth.feign.enabled=true
spring.sleuth.sampler.probability=1.0
spring.jpa.show-sql=true
spring.jpa.hibernate.use-new-id-generator-mappings=false
spring.redis.host=localhost
spring.redis.pool=6379
# 加入rabbitMQ的配置信息
spring.rabbitmq.addresses = localhost:5672
spring.rabbitmq.username = guest
spring.rabbitmq.password = guest
feign.hystrix.enabled=true
eureka.client.serviceUrl.defaultZone=http://localhost:8865/eureka/
eureka.client.registry-fetch-interval-seconds=5
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
hystrix.command.default.execution.timeout.enabled=true
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=3000
```

### Turbine服务端
在监控server端需要通过MQ接收Metrics，并在Turbine dashboard展示。这里新建立工程spring-cloud-turbine-mq来演示。
#### 添加依赖
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
        <artifactId>spring-cloud-scaffolding</artifactId>
        <groupId>me.peng</groupId>
        <version>1.0-SNAPSHOT</version>
   </parent>
  <artifactId>spring-cloud-turbine-mq</artifactId>
  <description>以MQ的方式监控hystrix情况</description>
  
  <dependencies>
  		<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- 添加turbine依赖 start -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-turbine-stream</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
        </dependency>
         <!-- 添加turbine依赖 end -->
    </dependencies>
</project>
```
#### 配置application.yml文件
```
spring:
  application:
    name: turbineservermq
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
server:
  port: 8870
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8865/eureka/
management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude: env
      cors:
        allowed-origins: "*"
        allowed-methods: "*"
```

#### 配置启动类
在启动的Application中增加 @EnableTurbineStream 注解，即可在启动后自动从queue中搜集监控信息。
```
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

```


## 演示 
启动eurka-server、userservice、auth-service、zuulserver、turbineserver、turbineservermq。

通过turbineserver项目访问：http://localhost:8867/hystrix ，输入http://localhost:8870/turbine.stream地址后，请求userservice的登录接口，可以看到如下信息:

![](https://user-gold-cdn.xitu.io/2019/8/1/16c4bf67ca4d67d5?w=783&h=391&f=png&s=38088)

同时登录rabbitMQ的管理界面：http://localhost:15672/#/ ,自动创建了一个 Exchange 和 Queue

![](https://user-gold-cdn.xitu.io/2019/8/1/16c4bf81a414be32?w=891&h=484&f=png&s=157606)

![](https://user-gold-cdn.xitu.io/2019/8/1/16c4bf84959c6bd7?w=899&h=489&f=png&s=133890)
