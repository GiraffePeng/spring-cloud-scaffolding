
# Spring Boot Admin 服务监控平台
## 1、介绍
Spring Boot Admin 是一个 管理 和 监控 Spring Boot 应用程序 的一款开源软件。Spring Boot Admin 分为 Server 端和 Client 端，Spring Boot Admin UI 部分使用 AngularJS 将数据展示在前端。
官方地址：[https://github.com/codecentric/spring-boot-admin/](https://github.com/codecentric/spring-boot-admin/)
大体功能如下：
* 显示 name/id 和版本号
* 显示在线状态
* Logging 日志级别管理
* JMX beans 管理
* Threads 会话和线程管理
* Trace 应用请求跟踪
* 应用运行参数信息，如：Java 系统属性、Java 环境变量属性、内存信息、Spring 环境属性
 
## 2、构建
### 2.1、构建Admin Server
基于之前项目，创建一个新的子级项目(spring-cloud-admin-server),其pom.xml依赖如下：
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>spring-cloud-scaffolding</artifactId>
        <groupId>me.peng</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>spring-cloud-admin-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-server</artifactId>
            <version>2.0.3</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
    </dependencies>

</project>
```
其中spring-boot-admin-starter-server为引入Spring Boot Admin的核心依赖包。
配置属性application.yml
```
server:
  port: 8868

spring:
  application:
    name: adminserver
  zipkin:
      base-url: http://localhost:9411
  sleuth:
    feign:
      enabled: true
    sampler:
      probability: 1.0

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8865/eureka/
    registry-fetch-interval-seconds: 5

management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude: env
  endpoint:
    health:
      show-details: always
```
启动类加上注解@EnableAdminServer
```
package com.peng.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableAdminServer  //启动类加上该注解标明开启Spring Boot Admin
@EnableDiscoveryClient
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( AdminServerApplication.class, args );
    }
}

```

至此Admin Server 创建完成。

### 2.2 构建Admin Client
因为使用的为Spring Cloud环境，那么其实并不用通过 Spring Boot Admin Client 来向 Spring Boot Admin 注册，而是让 Spring Boot Admin 通过注册中心（Eureka、Consul 等）来发现服务.
在admin-client中只需要引入spring-boot-starter-actuator包即可,以userservice为例
```
.....
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
 .....
```
## 3、验证
分别启动
* spring-cloud-eureka-server(注册中心)
* spring-cloud-admin-server(Spring Boot Admin服务端)
* spring-cloud-zuul-server(网关,在这里充当Admin Client)
* spring-cloud-userservice-server(用户业务服务，这里充当Admin Client)

启动后访问：http://localhost:8868/ 进入Spring Boot Admin页面，可以看到有两个服务正常运行，UserServer为Down状态，是因为没有启动MQ，导致UserServer无法连接发出报警信息。 
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820141658271.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
当我们把UserServer服务停止后，SBA（Spring Boot Admin）检测到UserServer服务不可用，会将其至为OFFLINE脱机状态
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820142042641.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
当我们将MQ启动后，重新启动UserServer服务,可以看到UserServer变成了UP，同时右下角还有服务状态变更的弹框提示。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820143459224.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
点击Journal，可以看到各个Client客户端的服务事件变更的时间点以及变更内容
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820143659553.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
点击Wallboard节点，可以看到有哪些客户端被抓取到客户端信息。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019082014391219.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
单击其中一个服务，比如USERSERVICE服务，进入到监控详情页，
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820144052388.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820144101727.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
左侧导航栏介绍：
* Details:客户端的详细信息，大体包括：Health健康状态(包括redis、磁盘、数据库、注册中心、熔断器等是否正常的提示)、进程、线程、内存堆、内存非堆空间的监控
* Environment：该服务所处的环境，大体包括：系统参数、系统环境、配置文件参数、服务端口等
* Configuration Properties：该客户端服务所有的配置参数项，包含application.yml中的以及默认的配置项。
* Logging：日志级别的修改
* JVM.Threads：线程的监控
* Web.Mappings：该客户端服务对外暴露的接口地址以及参数以及请求方式。
## 4、安全配置，在Spring Boot Admin中添加安全登陆界面
### 4.1、SBA服务端的改造
Spring Boot Admin 提供了登录界面的组件，并且和 Spring Boot Security 结合使用，需要 用户登录 才能访问。在 Admin Server 的 pom.xml 文件中引入以下依赖：
```
.....
<dependency>
    		<groupId>org.springframework.boot</groupId>
    		<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
.....
```
在 admin-server 模块的 application.yml 中完成如下配置，创建一个 security 的 user 用户，它的用户名为 admin，密码为 admin。通过 eureka.instance.metadate-map 配置属性 带上该 security 的 user 用户信息。

```
server:
  port: 8868

spring:
  application:
    name: adminserver
  security:
    user:
      name: 'admin'
      password: 'admin'
  zipkin:
      base-url: http://localhost:9411
  sleuth:
    feign:
      enabled: true
    sampler:
      probability: 1.0

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8865/eureka/
    registry-fetch-interval-seconds: 5
  instance:
    metadata-map:
      user.name: ${spring.security.user.name}
      user.password: ${spring.security.user.password}

management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude: env
  endpoint:
    health:
      show-details: always

```
然后在 应用程序 中配置 Spring Boot Security，创建一个 SecurityConfig 的 配置类，给 静态资源 加上 permitAll() 权限，其他的 资源访问 则需要 权限认证，另外这些资源不支持 CSFR（跨站请求伪造），所以禁用掉 CSFR，最后需要开启 HTTP 的基本认证，即 httpBasic() 方法。
```
package com.peng.admin.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

@Configuration
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

    private final String adminContextPath;

    public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");

        http.authorizeRequests().antMatchers("/login.html", "/**/*.css", "/img/**", "/third-party/**","/actuator/**")
        .permitAll()
                .antMatchers(adminContextPath + "/assets/**").permitAll()
                .antMatchers(adminContextPath + "/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and()
                .logout().logoutUrl(adminContextPath + "/logout").and()
                .httpBasic().and()
                .csrf().disable();
    }

}
```
### 4.2 验证
重新启动SBA服务端项目，可以看到有登陆界面需要进行登录才能进行访问，输入账号密码 admin admin 即可登陆。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820151226669.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1NTUxMDg5,size_16,color_FFFFFF,t_70)
集成登陆可以通过如下三种方法：
* basic-auth
* spring-session
* oauth2

详情请查看  [https://github.com/joshiste/spring-boot-admin-samples](https://github.com/joshiste/spring-boot-admin-samples)
