## eureka单点版

基于spring-cloud-scaffolding中pom的配置,在eureka-server的pom中添加如下配置:
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

    <artifactId>spring-cloud-eureka-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>

</project>
```
在resources文件夹下创建一个配置文件application.yml（对于Spring Cloud项目由于配置项有很多，为了能够使模块感层次感强一点，使用yml格式,当然也可以换成properties格式）：
```
server:
  port: 8865

eureka:
  instance:
    hostname: localhost
  client:
    registry-fetch-interval-seconds: 5
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://localhost:8865/eureka/
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 5000

spring:
  application:
    name: eurka-server
```
随后建立一个主程序文件：
```
package com.peng.eureka.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( EurekaServerApplication.class, args );
    }
}
```
对于搭建Spring Cloud的一些基础组件的服务，往往就是三步，加依赖，加配置，加注解开关即可。

启动后,访问页面如下：
![](https://user-gold-cdn.xitu.io/2019/6/28/16b9d139f0b9aec8?w=1901&h=958&f=png&s=117083)

## eureka集群版
基于以上单机版的配置，做下改造，即可满足集群版的要求。 集群版的理念在于eureka之间相互注册与发现。

在yml中添加集群的配置信息:
```
#单点模式
server:
  port: 8865

eureka:
  instance:
    hostname: localhost
  client:
    registry-fetch-interval-seconds: 5
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://localhost:8865/eureka/
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 5000

spring:
  application:
    name: eurka-server

---
## 集群模式
spring:
  application:
    name: eurka-server
  profiles: cluster1

server:
  port: 8861

eureka:
  instance:
    hostname: cluster1
  client:
    registry-fetch-interval-seconds: 5
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://cluster2:8862/eureka/,http://cluster3:8863/eureka/
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 5000
---
spring:
  application:
    name: eurka-server
  profiles: cluster2

server:
  port: 8862

eureka:
  instance:
    hostname: cluster2
  client:
    registry-fetch-interval-seconds: 5
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://cluster1:8861/eureka/,http://cluster3:8863/eureka/
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 5000
---
spring:
  application:
    name: eurka-server
  profiles: cluster3

server:
  port: 8863

eureka:
  instance:
    hostname: cluster3
  client:
    registry-fetch-interval-seconds: 5
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://cluster1:8861/eureka/,http://cluster2:8862/eureka/
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 5000
```
格外需要注意的有两个配置:
* eureka.client.registerWithEureka:表示是否将自己注册到eureka server，因为要构建集群环境，需要将自己注册到及群众，所以应该开启。默认为true，可不显式设置。
* eureka.client.fetchRegistry:表示是否从eureka server获取注册信息，如果是单一节点，不需要同步其他eureka server节点，则可以设置为false，但此处为集群，应该设置为true，默认为true，可不设置。
* eureka.client.serviceUrl.defaultZone:集群模式下,默认的注册于集群中的其他服务上。

因为要在一台机器上搭建集群环境，所以修改hosts文件,windows下hosts默认地址为:C:\Windows\System32\drivers\etc,调整完保存后即可生效。
```
127.0.0.1 cluster1
127.0.0.1 cluster2
127.0.0.1 cluster3
```
集群模式本机启动方式有两种：
### 以命令行模式启动
使用maven打成jar包后,使用命令
```
集群cluster1启动： java -jar spring-cloud-eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=cluster1
对应服务：http://localhost:8861/eureka/

集群cluster2启动： java -jar spring-cloud-eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=cluster2
对应服务：http://localhost:8862/eureka/

集群cluster3启动： java -jar spring-cloud-eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=cluster3
对应服务：http://localhost:8863/eureka/
```

### 以eclipse为例启动(IDEA同样,去调整启动时配置即可)
![](https://user-gold-cdn.xitu.io/2019/6/28/16b9d1b71c083900?w=848&h=749&f=png&s=39413)

在启动参数中加入对应的--spring.profiles.active 
分别启动三次:--spring.profiles.active=cluster1、--spring.profiles.active=cluster2、--spring.profiles.active=cluster3

集群模式启动后eureka管理界面如下(访问任一节点即可，此处访问cluster3):
![](https://user-gold-cdn.xitu.io/2019/6/28/16b9d213bf43e210?w=1905&h=954&f=png&s=129785)
可以看到画圈部分,已经将三个节点全部相互注册到eureka下。


## 小计
集群模式下,启动单个节点时,控制台或者日志会打印如下错误:
![](https://user-gold-cdn.xitu.io/2019/6/28/16b9d22803860cc8?w=1673&h=804&f=png&s=236783)

出现这种情况的原因是，我们的程序已经启动（已经出现红框中此条日志，代表程序已经启动，所以程序本身没有问题。），为什么会出现错误呢？

是因为在集群环境中，每一台服务器启动之后，都要去连接集群中的其他服务器，以便于相互之间通讯传递信息。
但是，集群服务器是按照次序启动的，不管先启动哪一台服务器，其他的服务器都还没有准备就绪，所以会出现找不到要连接的服务器，因此会报错。
等到所有的服务器全部启动，整个集群就可以正常运行。
