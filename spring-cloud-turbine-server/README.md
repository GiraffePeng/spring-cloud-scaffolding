# 断路器监控服务
## Hystrix Dashboard简介
在微服务架构中为了保证程序的可用性，防止程序出错导致网络阻塞，出现了断路器模型。断路器的状况反应了一个程序的可用性和健壮性，它是一个重要指标。Hystrix Dashboard是作为断路器状态的一个组件，提供了数据监控和友好的图形化界面。针对Hystrix进行实时监控的工具，通过Hystrix Dashboard我们可以在直观地看到各Hystrix Command的请求响应时间, 
## Turbine简介
hystrix只能实现单个微服务的监控，可是一般项目中是微服务是以集群的形式搭建，一个一个的监控不现实。而Turbine的原理是，建立一个turbine服务，并注册到eureka中，并发现eureka上的hystrix服务。通过配置turbine会自动收集所需hystrix的监控信息，最后通过dashboard展现，以达到集群监控的效果。

简单来说，就是通过注册到注册中心，发现其他服务的hystrix服务，然后进行聚合数据，最后通过自身的端点输出到仪表盘上进行个性化展示。这样我们就监控一个turbine应用即可，当有新增的应用加入时，我们只需要配置下turbine参数即可。

当然，我们还可以通过Turbine Stream的功能让客户端主动上报数据（通过消息队列）

## 通过 HTTP 收集聚合集成

### 结合turbine形成的架构图 
![](https://user-gold-cdn.xitu.io/2019/8/1/16c4b32d2bed60d6?w=875&h=334&f=png&s=82080)
### 引入依赖
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

    <artifactId>spring-cloud-turbine-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- 引入断路器相关包 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <!-- 引入hystrix-dashboard断路器数据监控 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <!-- 引入hystrix-turbine 汇总Hystrix服务断路器监控流-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
        </dependency>
    </dependencies>

</project>
```
### 配置application.yml文件
```
server:
  port: 8867

spring:
  application:
    name: turbineserver

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
  endpoint:
    health:
      show-details: always

turbine:
  aggregator:
    clusterConfig: default
# 集群名称，当我们服务数量非常多的时候，可以启动多个 Turbine服务来构建不同的聚合集群，
# 而该参数可以用来区分这些不同的聚合集群，同时该参数值可以在 Hystrix仪表盘中用来定位不同的聚合集群，
# 只需要在 Hystrix Stream 的 URL 中通过 cluster 参数来指定；
  clusterNameExpression: "'default'"
# true 同一主机上的服务通过host和port的组合来进行区分，默认为true
# false 在本机测试时 监控中host集群数会为1了 因为本地host是一样的
  combine-host: true
  instanceUrlSuffix:
    default: actuator/hystrix.stream
#需要监控的应用名称，默认逗号隔开
  app-config: investservice,userservice,projectservice,projectservice-listener
```
Turbine服务我们使用8867端口，这里重点看一下turbine下面的配置项

* turbine.instanceUrlSuffix配置了默认情况下每一个实例监控数据流的拉取地址
* turbine.app-config配置了所有需要监控的应用程序
* turbine.cluster-name-expression 参数指定了集群名称为 default，当我们服务数量非常多的时候，可以启动多个 Turbine 服务来构建不同的聚合集群，而该参数可以用来区分这些不同的聚合集群，同时该参数值可以在 Hystrix 仪表盘中用来定位不同的聚合集群，只需要在 Hystrix Stream 的 URL 中通过 cluster 参数来指定；注意：集群名称这个一定要用 String 来包一下，new String("default")或者"'default'"，否则启动的时候会抛出异常：
```
org.springframework.expression.spel.SpelEvaluationException: EL1008E: Property or field 'default' cannot be found on object of type 'com.netflix.appinfo.InstanceInfo' - maybe not public or not valid?
```
* turbine.combine-host-port参数设置为true，可以让同一主机上的服务通过主机名与端口号的组合来进行区分，默认情况下会以 host 来区分不同的服务，这会使得在本地调试的时候，本机上的不同服务聚合成一个服务来统计。


这里的Turbine其实是从各个配置的服务读取监控流来汇总监控数据的，并不是像Zipkin这种由服务主动上报数据的方式。

### 配置启动类
```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@EnableTurbine //开启turbine服务
public class TurbineServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( TurbineServerApplication.class, args );
    }
}
```
## 被监控服务集成
现在我们的仪表盘工程已经创建成功了，但是还不能用来监控某一个服务，要监控某一个服务，需要该服务提供一个/hystrix.stream接口，我们需要对我们的需要被监控的服务改造。

以userserver为例，进行改造。
### 引入依赖
```
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```
### 配置启动类加入注解
```
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableHystrix
//服务消费者工程的入口类上添加@EnableCircuitBreaker注解，表示开启断路器功能
@EnableCircuitBreaker
@Configuration
@EnableFeignClients
@EntityScan("com.peng.userservice")
public class UserServiceApplication {
    @Bean
    RedissonClient redissonClient() {
        return Redisson.create();
    }
    public static void main(String[] args) {
        SpringApplication.run( UserServiceApplication.class, args );
    }
}
```

## 演示
启动eurka-server、userservice、auth-service、zuulserver、turbineserver。

针对userserver加入了断路器功能后，访问/hystrix.stream接口，得先访问userservice工程中的任意一个其他接口，否则如果直接访问/hystrix.stream接口的话，会打印出一连串的ping: ping: ...。

启动后我们先访问http://localhost:8866/user/user/login(登陆接口，其中利用feign调用了auth-service的/oauth/token接口)，后访问http://localhost:8866/user/actuator/hystrix.stream路径，可以看到类似如下输出：

![](https://user-gold-cdn.xitu.io/2019/7/30/16c423c26a6f3be6?w=1906&h=688&f=png&s=166238)

并且会不断刷新以获取实时的监控数据。但是纯文字的输出可读性实在是太差，这时就需要Hystrix Dashboard可以可视化查看实时监控数据。

通过turbineserver项目访问：http://localhost:8867/hystrix， 访问界面如下：
![](https://user-gold-cdn.xitu.io/2019/7/30/16c40ee3ff62f12b?w=1905&h=652&f=png&s=158385)
针对监控服务地址，一共有三种数据源形式，即不同的监控方式：
* 默认的集群监控：通过URL：http://turbine-hostname:port/turbine.stream 开启，实现对默认集群的监控。
* 指定的集群监控：通过URL：http://turbine-hostname:port/turbine.stream?cluster=[clusterName] 开启，实现对clusterName集群的监控。
* 单体应用的监控：通过URL：http://hystrix-app:port/actuator/hystrix.stream 开启，实现对具体某个服务实例的监控。

### 针对单体应用的监控
在地址栏中输入上述的userserver的/hystrix.stream接口，http://localhost:8866/user/actuator/hystrix.stream ，展示的内容如下图所示：
![](https://user-gold-cdn.xitu.io/2019/7/31/16c45b0a1c9ea5e1?w=975&h=525&f=png&s=72432)

### 针对集群的监控
同样在(http://localhost:8867/hystrix)的地址栏中输入http://localhost:8867/turbine.stream进入监控面板。

多调用几次http://localhost:8866/invest/createInvest接口,该接口涉及多个项目间的feign调用，面板展示结果如下：

![](https://user-gold-cdn.xitu.io/2019/8/1/16c4b305eae99416?w=1348&h=422&f=png&s=61792)
可以看到通过turbine可以轻松的整合所要监控的服务的接口调用状态。

