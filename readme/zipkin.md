# 服务调用全链路跟踪
针对调用链路跟踪，Spring Cloud的组件Sleuth通过集成zipkin提供了一种解决方案，本文主要讲述Sleuth+ZipKin的集成。Spring Cloud Sleuth 的主要功能就是为 分布式系统提供追踪解决方案，并且兼容支持了Zipkin，只需要在pom.xml文件中引入相应的依赖即可。它主要用于聚集来自各个异构系统的实时监控数据，用来追踪微服务架构下的系统延时问题。

## 1、基本术语
### 1.1、Span
Span 是一个基本的 工作单元，用于描述一次 RPC 调用，Span 通过一个 64 位的 spanId 作为 唯一标识。Zipkin 中的 Span 还有其他数据信息，比如 摘要、时间戳事件、关键值注释 (tags) 以及 进度 ID (通常是 IP 地址)。Span 在不断的启动和停止，同时记录了 时间信息，一个 Span 创建后，必须在未来的某个时刻停止它。

### 1.2、Trace
Trace：一系列spans组成的一个树状结构，例如，如果你正在跑一个分布式大数据工程，你可能需要创建一个trace。

### 1.3、Annotation
表示基本标注列表，一个Annotation可以理解成Span生命周期中重要时刻的数据快照，比如一个Annotation中一般包含发生时刻（timestamp）、事件类型（value）、端点（endpoint）等信息。其中 Annotation 的 事件类型 包含以下四类：

* cs - Client Sent -客户端发起一个请求，这个annotion描述了这个span的开始
* sr - Server Received -服务端获得请求并准备开始处理它，如果将其sr减去cs时间戳便可得到网络延迟
* ss - Server Sent -注解表明请求处理的完成(当请求返回客户端)，如果ss减去sr时间戳便可得到服务端需要的处理请求时间
* cr - Client Received -表明span的结束，客户端成功接收到服务端的回复，如果cr减去cs时间戳便可得到客户端从服务端获取回复的所有所需时间

## 2、Spring Cloud Sleuth 
实际项目中若已引入Spring Could Sleuth包，通过上述的术语，则默认会在打印日志时输出traceId、spanId等内容，若它调用一个也是用Spring Could Sleuth的服务，则会自动传递traceId

类似这种traceId的传递操作，在服务内多采用ThreadLocal或MDC的方式，在分布式微服务时一般在request header或url参数中传递。 

## 3、Zipkin
Zipkin是一个开放源代码分布式的跟踪系统，每个服务向zipkin报告计时数据，zipkin会根据调用关系通过Zipkin UI生成依赖关系图。
Zipkin提供了可插拔数据存储方式：In-Memory、MySql、Cassandra以及Elasticsearch。为了方便在开发环境我直接采用了In-Memory方式进行存储，生产数据量大的情况则推荐使用Elasticsearch。

如果一个服务的调用关系如下:

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f0a63748d260?w=957&h=178&f=png&s=18571)
那么此时将Span和Trace在一个系统中使用Zipkin注解的过程图形化： 

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f0adfbad7dda?w=929&h=484&f=png&s=87851)

## 4、Sleuth + Zipkin 工程搭建
### 4.1、版本选择：
```
Spring Cloud - Finchley.RELEASE 
Spring Boot - 2.0.3.RELEASE 
Zipkin - 2.11.6
```
### 4.2、项目准备:
源码：https://github.com/GiraffePeng/spring-cloud-scaffolding
* spring-cloud-eureka-server注册中心，端口8865
* spring-cloud-zuul-server服务网关，端口8866
* spring-cloud-auth-server授权服务，端口8599
* spring-cloud-userservice-server用户业务服务，端口8761

上述4个项目都在github的源码中能够找到，本次链路跟踪主要模拟的场景是通过网关访问user用户服务，然后进行调用auth服务授权并且登陆与注册的场景。

因为使用的Spring Boot的版本为2.0版本以上，zipkin官方建议不进行自己集成，而是通过使用官方提供的zipkin的jar包进行部署zipkin项目。
* 搭建方式详见官网 ：[Github:zipkin](https://github.com/openzipkin/zipkin)
* jar包下载地址：[zipkin的jar包下载](https://dl.bintray.com/openzipkin/maven/io/zipkin/java/zipkin-server/)

此处使用2.11.6版本。

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f1653989268d?w=501&h=617&f=png&s=22746)
### 4.3、项目的改造
我们需要在被Zipkin监控的服务上进行改造，达到能够通过Sleuth将各种上述术语提到的节点主动上报Zipkin的功能。

这里需要被监控的是spring-cloud-zuul-server、spring-cloud-auth-server、spring-cloud-userservice-server

#### 4.3.1 引入依赖
在上述的三个需要被监控的项目pom.xml中加入zipkin的引入。
```
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```
#### 4.3.2 配置属性
同样，在上述三个项目的application.yml或者applicaiton.properties中加入对zipkin的支持，以userservice为例(只展示部分的配置属性，全部的可查询源码)。

```
server:
  port: 8761
spring:
  application:
    name: userservice
  client:
    service-url:
      defaultZone: http://localhost:8865/eureka/
  sleuth:
    sampler:
      percentage: 1.0
  zipkin:
    # zipkin的连接地址
    base-url: http://localhost:9411/
    # 若在同一个注册中心的话可以启用自动发现，省略base-url
    # locator:
      # discovery:
        # enabled: true #自动发现
```
这里说明下配置的关键属性
* spring.sleuth.sampler.percentage: 链路调用的收集率，全链路监控抽样概率100%，也就是1.0（默认10%，丢数据太多不方便观察结果）
* spring.zipkin.base-url：需要连接的zipkin服务的地址，默认端口9411
* spring.zipkin.locator.discovery.enabled: 若在同一个注册中心可以启动自动发现，省略base-url，这里因为使用的官方提供的jar包，默认不会注册到自己搭建的注册中心，所以不使用该方式。

到这里可以发现，引入ZipKin服务只需要导依赖、配置属性，两步即可。

### 4.4 启动验证
ok，基本上的改造大功告成，接下来进行测试。
#### 4.4.1 启动zipkin的jar包
windows的话打开cmd，转移到jar包相应的目录下，录入命令:
```
java -jar zipkin-server-2.11.6-exec.jar
```
启动后命令窗口展示如下：
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f274e86554be?w=826&h=425&f=png&s=152942)

#### 4.4.2 启动本身服务
依次启动准备的4个项目，eureka、zuul、auth、user，启动后为了形成链路，需要调用接口。

这里调用用户的登录接口。
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f2e3634dc2d3?w=1489&h=815&f=png&s=161063)
这里调用用户的注册接口。
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f2f7993ee53d?w=1484&h=584&f=png&s=67561)

#### 4.4.3 验证
在浏览器上输入zipkin的访问地址：http://localhost:9411/zipkin/ ，打开的界面如下：
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f35432da0055?w=1918&h=952&f=png&s=83212)
先通过默认的点击Find Traces按钮，可以看到如下信息:
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f376bcf0e2fd?w=1878&h=520&f=png&s=50110)
点击其中一个链路，会展示该链路的详细信息，包括请求的达到服务的顺序，每个服务的耗时，每个服务上调用的内容等：
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f3878d558c5d?w=1900&h=456&f=png&s=43462)
点击其中的一个服务，还可以看到更详细的信息
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f396d7dd7c20?w=1890&h=799&f=png&s=111793)

还有一种场景，就比如我们通过日志查看该条日志的链路地址怎么办？
本场景下的入口服务为zuul，打开zuul的日志，因为使用了Sleuth,它会格式化我们服务的日志

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f3d7a5973f7e?w=1883&h=518&f=png&s=185398)
红圈中的为 注册接口的日志， 通过服务名称、traceId、spanId、是否发送至zipkin为格式打印日志，只需要找到你想要查看的traceId在zipkin上根据traceId搜索即可。


同时zipkin还提供了服务之间的调用链路的展示。
可以看到zuul通过路由分发至user，然后user通过feign调用了auth。

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f3fb87ddb9cc?w=1906&h=286&f=png&s=31264)

## 5、Sleuth + Kafka + Zipkin + Elasticsearch + Kibana 全链路监控
### 5.1、介绍
上述的4节点展示的zipkin的链路跟踪不适用于实际环境中，因为刚才提到zipkin默认的链路存储方式为内存存储，非常容易数据丢失，无法达到持久化的目的。zipkin还提供了常用的mysql或者ES存储，在生产环境流量大的体系下，使用mysql存储链路信息不如ES，故选择ES来存储链路信息。

### 5.2、逻辑图

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f4ccaeff8d38?w=1262&h=437&f=png&s=31815)
被监控的服务通过sletuh主动上报给kafka链路的信息的JSON串，zipkin通过kafka去收集链路信息，并通过ES来存储链路信息，kibana通过存储的链路信息来展示链路的信息。

### 5.3、准备环境
因为需要kafka、es、kibana等，我们需要进行安装，这里不提及安装与运行步骤，有不了解的小伙伴请通过查询资料准备环境。

### 5.4、本文所选版本
* Kafka - 2.1.1
* Zookeeper - 3.4.13
* Elasticsearch - 5.6.8
* Kibana - 5.6.8

### 5.5 改造项目
同样，我们需要改造那三个项目，spring-cloud-zuul-server、spring-cloud-auth-server、spring-cloud-userservice-server。
#### 5.5.1 pom.xml
```
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
<dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
</dependency>
```
与第四节对比新引入了kafka的依赖，用于服务通过sleuth上报给kafka链路信息。

#### 5.5.2 配置属性
三个项目同样需要修改application.yml，这里还是以userservce为例：
```
server:
  port: 8761
spring:
  application:
    name: userservice
  client:
    service-url:
      defaultZone: http://localhost:8865/eureka/
  sleuth:
    sampler:
      percentage: 1.0
  zipkin:
#    base-url: http://localhost:9411/
    sender:
      type: kafka
    kafka:
      topic: zipkin
  kafka:
    bootstrap-servers: localhost:9092
```
* spring.zipkin.sender.type:指明了zipkin的获取数据源是什么
* spring.kafka.bootstrap-servers: 指明了kafka的连接地址

需要改造的一是引入依赖、二是调整属性的配置。
### 5.6 启动验证
#### 5.6.1 启动
这里启动如下项目：
* spring-cloud-eureka-server
* spring-cloud-zuul-server
* spring-cloud-auth-server
* spring-cloud-userservice-server
* zookeeper
* kafka

这里先启动这些，我们进行验证服务能否通过sleuth主动上发至kafka。
在kafka安装目录下的bin/windows目录下打开cmd输入命令：
```
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic zipkin --from-beginning
```
意思是通过命令行来监听消费kafka的topic名为zipkin的数据。

同样调用用户的登录接口，查看cmd窗口的打印结果，可以看到每当发生请求时，窗口中就会打印出链路的信息。

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f60fcab23107?w=1903&h=559&f=png&s=163247)
格式化后如下：
```
[{
	"traceId": "a9e7c1f813a41c7f",
	"parentId": "a9e7c1f813a41c7f",
	"id": "971e08a4542e4ee7",
	"kind": "CLIENT",
	"name": "post",
	"timestamp": 1565773573291289,
	"duration": 734207,
	"localEndpoint": {
		"serviceName": "zuulserver",
		"ipv4": "192.168.241.238"
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/user/login"
	}
}, {
	"traceId": "a9e7c1f813a41c7f",
	"id": "a9e7c1f813a41c7f",
	"kind": "SERVER",
	"name": "post",
	"timestamp": 1565773573284757,
	"duration": 746710,
	"localEndpoint": {
		"serviceName": "zuulserver",
		"ipv4": "192.168.241.238"
	},
	"remoteEndpoint": {
		"ipv6": "::1",
		"port": 59932
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/user/user/login"
	}
}]
[{
	"traceId": "a9e7c1f813a41c7f",
	"parentId": "f2c9a2ae2de2d917",
	"id": "23c6ce0ccda5a105",
	"kind": "SERVER",
	"name": "post",
	"timestamp": 1565773573739908,
	"duration": 267741,
	"localEndpoint": {
		"serviceName": "auth-service",
		"ipv4": "192.168.241.238"
	},
	"remoteEndpoint": {
		"ipv4": "10.10.128.142",
		"port": 59943
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/oauth/token"
	},
	"shared": true
}]
[{
	"traceId": "a9e7c1f813a41c7f",
	"parentId": "a9e7c1f813a41c7f",
	"id": "971e08a4542e4ee7",
	"kind": "SERVER",
	"name": "post /user/login",
	"timestamp": 1565773573294715,
	"duration": 734006,
	"localEndpoint": {
		"serviceName": "userservice",
		"ipv4": "192.168.241.238"
	},
	"remoteEndpoint": {
		"ipv6": "::1",
		"port": 59933
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/user/login",
		"mvc.controller.class": "UserServiceController",
		"mvc.controller.method": "login"
	},
	"shared": true
}]
[{
	"traceId": "caf2f4abb1e061ce",
	"parentId": "caf2f4abb1e061ce",
	"id": "82c1ad6fb1377d7d",
	"kind": "CLIENT",
	"name": "post",
	"timestamp": 1565773580687785,
	"duration": 476341,
	"localEndpoint": {
		"serviceName": "zuulserver",
		"ipv4": "192.168.241.238"
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/user/login"
	}
}, {
	"traceId": "caf2f4abb1e061ce",
	"id": "caf2f4abb1e061ce",
	"kind": "SERVER",
	"name": "post",
	"timestamp": 1565773580680910,
	"duration": 485492,
	"localEndpoint": {
		"serviceName": "zuulserver",
		"ipv4": "192.168.241.238"
	},
	"remoteEndpoint": {
		"ipv6": "::1",
		"port": 59932
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/user/user/login"
	}
}]
[{
	"traceId": "caf2f4abb1e061ce",
	"parentId": "e531528a2163c612",
	"id": "1b20697fb36ae62c",
	"kind": "SERVER",
	"name": "post",
	"timestamp": 1565773580909433,
	"duration": 251063,
	"localEndpoint": {
		"serviceName": "auth-service",
		"ipv4": "192.168.241.238"
	},
	"remoteEndpoint": {
		"ipv4": "10.10.128.142",
		"port": 59964
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/oauth/token"
	},
	"shared": true
}]
[{
	"traceId": "caf2f4abb1e061ce",
	"parentId": "e531528a2163c612",
	"id": "1b20697fb36ae62c",
	"kind": "CLIENT",
	"name": "post",
	"timestamp": 1565773580905822,
	"duration": 254009,
	"localEndpoint": {
		"serviceName": "userservice",
		"ipv4": "192.168.241.238"
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/oauth/token"
	}
}, {
	"traceId": "caf2f4abb1e061ce",
	"parentId": "82c1ad6fb1377d7d",
	"id": "e531528a2163c612",
	"name": "hystrix",
	"timestamp": 1565773580904638,
	"duration": 258836,
	"localEndpoint": {
		"serviceName": "userservice",
		"ipv4": "192.168.241.238"
	}
}, {
	"traceId": "caf2f4abb1e061ce",
	"parentId": "caf2f4abb1e061ce",
	"id": "82c1ad6fb1377d7d",
	"kind": "SERVER",
	"name": "post /user/login",
	"timestamp": 1565773580692959,
	"duration": 471547,
	"localEndpoint": {
		"serviceName": "userservice",
		"ipv4": "192.168.241.238"
	},
	"remoteEndpoint": {
		"ipv6": "::1",
		"port": 59933
	},
	"tags": {
		"http.method": "POST",
		"http.path": "/user/login",
		"mvc.controller.class": "UserServiceController",
		"mvc.controller.method": "login"
	},
	"shared": true
}]
```
可以看到链路的信息已经被发送至kafka。
那么接下里继续启动项目
* Zipkin
* Elasticserach
* Kibana

这里zipkin可以通过参数来规定启动的方式，以本文的环境下为例就是：
```
java -DKAFKA_BOOTSTRAP_SERVERS=localhost:9092 -DSTORAGE_TYPE=elasticsearch -DES_HOSTS=localhost:9200 -jar zipkin-server-2.11.6-exec.jar
```
* -DKAFKA_BOOTSTRAP_SERVERS:声明了kafka的连接地址
* -DSTORAGE_TYPE：声明了链路信息的存储类型
* -DES_HOSTS=声明了Elasticsearch的连接地址

全部启动后，我们进行验证

#### 5.6.2 验证
调用用户登录接口，同样查看zipkin的界面
##### 5.6.2.1 验证zipkin
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f6830a49e0a1?w=1920&h=923&f=png&s=103005)
可以看到这条信息为不到1分钟前创建的。

PS: 这里zipkin因为使用了ES进行链路信息的存储，所以在dependences节点下展示不出来服务的链路调用关系。
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f7169655d274?w=1359&h=488&f=png&s=31927)
这个在zipkin的官网上有提及，如果使用ES存储，需要展示出服务的链路关系的话，需要自动部署zipkin的插件（zipkin-dependencies-xxx.jar），启动该jar包后就可以针对当天的链路信息展示调用关系，想要了解的小伙伴可以查阅资料，这里不过多描述。


##### 5.6.2.1 验证ES
我们打开ES的head查看es中的信息，浏览器输入默认的端口：http://localhost:9100/
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f697088fd0af?w=742&h=288&f=png&s=43094)
可以看到zipkin在es中创建了zipkin:span-yyyy-MM-dd格式的索引文件。

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f6a9b6b2c9e8?w=1917&h=919&f=png&s=329664)
我们切换到数据查看节点，可以看到每个索引中的数据信息和刚才kafka监听打印的json格式的能够对应上。

##### 5.6.2.1 验证Kibana
Kibana启动后，在浏览器上输入地址：http://localhost:5601，因为首次启动需要配置索引.

![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f6d308a7e53c)
点击Management去创建配置到zipkin*的索引，最后点击create,配置成功后如下图所示：
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f6e02fe2a9dd?w=1912&h=947&f=png&s=156416)

点击Discover节点，可以看到一条条的链路信息在该处展示出来。
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f6ea673324e8?w=1914&h=951&f=png&s=217463)
点击一条日志信息，可以看到内容与ES中所存储的格式和内容一致。
![](https://user-gold-cdn.xitu.io/2019/8/14/16c8f6fb992f9b18?w=1448&h=727&f=png&s=95444)

## 6、总结
* 通过Spring Cloud的Sleuth + Zipkin能够很方便的实现链路的跟踪。
* 对于生产环境强烈建议使用ES或者Mysql进行存储。
* 对于集成链路，只需要在原有的项目上加入依赖并且配置属性即可，对代码的侵入很少。