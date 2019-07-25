# 网关服务
* 路由分发,作为所有接口请求入口，负责进行请求的流量转发
* 动态路由配置与更新，可以将路由配置移至redis或者DB中，达到实时改变路由配置。
* Ribbon均衡负载
* 统一的zuul网关层异常处理，方便调用方统一的异常数据解析处理
* 请求限流,将阈值写入配置，动态配置限流的阈值大小
* 服务层级熔断降级，服务宕机后，可及时进行熔断处理，防止服务器雪崩
* Swagger API文档，在线实时生成接口文档以及接口测试，
* zuul过滤器,可在网关层加入各种过滤器，达到转发实际服务器前的一系列操作
* zuul网关层身份认证(oauth2.0),作为资源认证服务，将需要带有令牌访问的接口进行认证，如果不符合要求，进行返回错误信息
## 一、搭建网关服务
搭建最基本的zuul网关
* 1、配置pom.xml，添加spring-cloud-starter-zuul的依赖
* 2、配置application.yml，设置分流操作
* 3、配置启动类，添加@EnableZuulProxy注解，开启动态路由
* 4、配置过滤器，实现ZuulFilter抽象类
* 5、配置过滤器到启动类。
第4和第5步，不是非必须的，而是自定义过滤器的操作

这里就不过多描述
## 二、动态路由配置与更新
在没有集成cloud或者apollo等配置中心时，无法做到配置的热更新，但是我们想动态的去更改zuul的路由信息时，可以采用以下方式:
### 从redis中获取路由信息
集成各种配置进行实例化，调用DynamicRouteLocator的构造函数，修改路由信息。
```
/**
 * 动态路由配置类
 */
@Configuration
public class DynamicRouteConfiguration {
    private Registration registration;
    private DiscoveryClient discovery;
    private ZuulProperties zuulProperties;
    private ServerProperties server;
    private RedisTemplate redisTemplate;

    public DynamicRouteConfiguration(Registration registration, DiscoveryClient discovery,
                                     ZuulProperties zuulProperties, ServerProperties server, RedisTemplate redisTemplate) {
        this.registration = registration;
        this.discovery = discovery;
        this.zuulProperties = zuulProperties;
        this.server = server;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public DynamicRouteLocator dynamicRouteLocator() {
        return new DynamicRouteLocator(server.getServlet().getServletPrefix()
                , discovery
                , zuulProperties
                , registration
                , redisTemplate);
    }
}
```
DynamicRouteLocator类集成DiscoveryClientRouteLocator，重写路由配置

ps:这里zuul网关会自动去轮询调用locateRoutesFromDb方法来获取最新的路由配置信息，无须关注是否要触发更新，只关注如何取路由信息和更新路由数据即可。
```
/**
 * 动态路由实现
 */
@Slf4j
public class DynamicRouteLocator extends DiscoveryClientRouteLocator {
    private ZuulProperties properties;
    private RedisTemplate redisTemplate;

    public DynamicRouteLocator(String servletPath, DiscoveryClient discovery, ZuulProperties properties,
                               ServiceInstance localServiceInstance, RedisTemplate redisTemplate) {
        super(servletPath, discovery, properties, localServiceInstance);
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 重写路由配置
     * <p>
     * 1. properties 配置。
     * 2. eureka 默认配置。
     * 3. DB数据库配置。
     * 4. redis自定义数据格式配置
     * @return 
     */
    @Override
    protected LinkedHashMap<String, ZuulProperties.ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<>();
        //读取properties配置、eureka默认配置
        routesMap.putAll(super.locateRoutes());
        log.debug("初始默认的路由配置完成");
        routesMap.putAll(locateRoutesFromDb());
        LinkedHashMap<String, ZuulProperties.ZuulRoute> values = new LinkedHashMap<>();
        for (Map.Entry<String, ZuulProperties.ZuulRoute> entry : routesMap.entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (StringUtils.isNotBlank(this.properties.getPrefix())) {
                path = this.properties.getPrefix() + path;
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
            }
            values.put(path, entry.getValue());
        }
        return values;
    }
    
	/**
     * Redis中保存的，没有从系统模块取，避免启动链路依赖问题（取舍），网关依赖业务模块的问题
     *
     * @return
     */
    private Map<String, ZuulProperties.ZuulRoute> locateRoutesFromDb() {
        Map<String, ZuulProperties.ZuulRoute> routes = new LinkedHashMap<>();
        //从redis中获取路由信息
        String obj = (String)redisTemplate.opsForValue().get("_ROUTE_KEY");
        if (obj == null) {
            return routes;
        }
	//转换为ZuulRoute集合
        List<ZuulRoute> results = JSONObject.parseArray(obj, ZuulRoute.class);
		//List<ZuulRoute> results = (List<ZuulRoute>) obj;
        //循环路由集合，给zuulRoute赋值，并装进Map集合中
	for (ZuulRoute result : results) {
            if (StringUtils.isBlank(result.getPath()) && StringUtils.isBlank(result.getUrl())) {
                continue;
            }
            ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
            try {
                zuulRoute.setId(result.getServiceId());
                zuulRoute.setPath(result.getPath());
                zuulRoute.setServiceId(result.getServiceId());
                zuulRoute.setRetryable(StringUtils.equals(result.getRetryable(), "0") ? Boolean.FALSE : Boolean.TRUE);
                zuulRoute.setStripPrefix(StringUtils.equals(result.getStripPrefix(), "0") ? Boolean.FALSE : Boolean.TRUE);
                zuulRoute.setUrl(result.getUrl());
                if (StringUtils.isNotBlank(result.getSensitiveheadersList())) {
                	String[] split = result.getSensitiveheadersList().split(",");
                	Set<String> sensitiveHeaderSet = new HashSet<String>();
                    for (int i = 0; i < split.length; i++) {
                    	sensitiveHeaderSet.add(split[i]);
					}
                    zuulRoute.setSensitiveHeaders(sensitiveHeaderSet);
                    zuulRoute.setCustomSensitiveHeaders(true);
                }
            } catch (Exception e) {
                log.error("从缓存加载路由配置异常", e);
            }
            log.debug("添加自定义的路由配置,path：{}，serviceId:{}", zuulRoute.getPath(), zuulRoute.getServiceId());
            routes.put(zuulRoute.getPath(), zuulRoute);
        }
        return routes;
    }
}
```
ZuulRoute路由实体类
```
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZuulRoute implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * router Id
     */
    private Integer id;
    /**
     * 路由路径
     */
    private String path;
    /**
     * 服务名称
     */
    private String serviceId;
    /**
     * url代理
     */
    private String url;
    /**
     * 转发去掉前缀
     */
    private String stripPrefix;
    /**
     * 是否重试
     */
    private String retryable;
    /**
     * 是否启用
     */
    private String enabled;
    /**
     * 敏感请求头
     */
    private String sensitiveheadersList;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标识（0-正常,1-删除）
     */
    private String delFlag;


}
```
### 在redis中放入路由信息
建立controller，注入RedisTemplate,提供接口放入路由信息,代码如下：
```
@RestController
public class ProjectServiceController {

    @Autowired
    RedisTemplate redisTemplate;
    
    private final static String ZUUL_ROUTE = "_ROUTE_KEY";

    /**
     * 通过传递参数修改redis中的路由信息
     * path：路由路径
     * serviceId：服务名称
     * stripPrefix：转发去掉前缀
     * enabled：是否启用
     * retryable：是否重试
     * 来动态变更路由消息。
     * @param zuulRoutes
     */
    @PostMapping("/zuulRoute/refresh")
    public void refreshZuulRoute(@RequestBody List<ZuulRoute> zuulRoutes){
    	for (ZuulRoute zuulRoute : zuulRoutes) {
    		if(StringUtils.isBlank(zuulRoute.getPath()) || StringUtils.isBlank(zuulRoute.getServiceId())) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
        	if(StringUtils.isBlank(zuulRoute.getStripPrefix()) || !(zuulRoute.getStripPrefix().equals("1") || zuulRoute.getStripPrefix().equals("0"))) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
        	if(StringUtils.isBlank(zuulRoute.getEnabled()) || !(zuulRoute.getEnabled().equals("1") || zuulRoute.getEnabled().equals("0"))) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
        	if(StringUtils.isBlank(zuulRoute.getRetryable()) || !(zuulRoute.getRetryable().equals("1") || zuulRoute.getRetryable().equals("0"))) {
        		throw new RuntimeException("路由更新传入的参数缺失");
        	}
		}
    	ValueOperations opsForValue = redisTemplate.opsForValue();
    	opsForValue.set(ZUUL_ROUTE, JSONObject.toJSONString(zuulRoutes));
    }
}
```
### 从关系型数据库中获取路由信息
除了使用redis，还可以使用关系型数据库获取路由信息，通过更改数据库表中的路由信息，进行动态加载。

以mysql数据库为例，数据库表结构如下：
```
CREATE TABLE `sys_zuul_route` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'router Id',
  `path` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '路由路径',
  `service_id` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '服务名称',
  `url` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'url代理',
  `strip_prefix` char(1) CHARACTER SET utf8mb4 DEFAULT '1' COMMENT '转发去掉前缀',
  `retryable` char(1) CHARACTER SET utf8mb4 DEFAULT '1' COMMENT '是否重试',
  `enabled` char(1) CHARACTER SET utf8mb4 DEFAULT '1' COMMENT '是否启用',
  `sensitiveHeaders_list` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '敏感请求头',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 DEFAULT '0' COMMENT '删除标识（0-正常,1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 COMMENT='动态路由配置表';
```
这里不在具体展示从数据库中如何获取路由，大体思路为，集成mybatis或者jpa，在DynamicRouteLocator类中查询该表的数据，进行转换为List\<ZuulRoute\>即可。
## 三、统一的zuul网关层异常处理
在没有对网关错误进行特殊处理时，通过网关调用其他服务出现错误会出现如下错误信息：
```
{
    "timestamp": "2019-07-11T06:11:46.557+0000",
    "status": 500,
    "error": "Internal Server Error",
    "message": "GENERAL"
}
```
当我们要求对返回结果有统一的成功或者失败格式时，这种错误返回格式就会不满足我们的需要，我们需要进行改造。
修改网关的异常返回信息的方式有很多，这里选择比较简单的，直接通过实现ErrorController接口，重写getErrorPath()方法，将至引导到自己实现的异常处理方法上，代码如下：
```
/**
 * zuul网关层统一的异常处理，错误返回格式的修改
 */
@RestController
public class ErrorHandler implements ErrorController{
	
    private final ErrorAttributes errorAttributes;
	 
    @Autowired
    public ErrorHandler(ErrorAttributes errorAttributes) {
    	this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ErrorResult error(HttpServletRequest request) {
    	WebRequest webRequest = new ServletWebRequest(request);
	Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(webRequest, true);
	String msg = errorAttributes.getOrDefault("error", "not found").toString();
	String code = errorAttributes.getOrDefault("status", 404).toString();
	return ErrorResult.builder().code(Integer.valueOf(code)).msg(msg).build();
    }
    
    //重写getErrorPath()方法，将至引导到自己实现的异常处理方法上
    @Override
    public String getErrorPath() {
    	return "/error";
    }
}
```
ErrorResult异常数据封装类
```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer code;
	
	private String msg;
}
```
重启zuul网关服务，调用接口报错提示内容如下：
```
{
    "code": 500,
    "msg": "Internal Server Error"
}
```

## 四、zuul的请求过滤器
zuul还能进行请求过滤，演这里示了一个授权校验的例子，检查请求是否提供了token参数，如果没有的话拒绝转发服务，返回401响应状态码和错误信息，首先我们需要先新建一个TokenFilter类来继承ZuulFilter这个类，实现它的四个接口
```
@Component
public class TokenFilter extends ZuulFilter {
	
	/**
	 * 四种类型：pre,routing,error,post
    pre：主要用在路由映射的阶段是寻找路由映射表的
    routing:具体的路由转发过滤器是在routing路由器，具体的请求转发的时候会调用
    error:一旦前面的过滤器出错了，会调用error过滤器。
    post:当routing，error运行完后才会调用该过滤器，是在最后阶段的
	 */
    @Override
    public String filterType() {
        return PRE_TYPE;
    }
    
    //自定义过滤器执行的顺序，数值越大越靠后执行，越小就越先执行
    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    //控制过滤器生效不生效，可以在里面写一串逻辑来控制
    @Override
    public boolean shouldFilter() {
        return true;
    }
    
    //执行过滤逻辑
    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        
        HttpServletRequest request = ctx.getRequest();
        String token = request.getParameter("token");
        if(token == null) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().setCharacterEncoding("UTF-8");
                ctx.getResponse().getWriter().write("禁止访问");
            } catch (Exception e){}

            return null;
        }
        return null;
    }
}
```
filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下：
* 1.pre：可以在请求被路由之前调用，用在路由映射的阶段是寻找路由映射表的
* 2.route：在路由请求时候被调用，具体的路由转发过滤器是在routing路由器具体的请求转发的时候会调用
* 3.error：处理请求时发生错误时被调用
* 4.post：当routing，error运行完后才会调用该过滤器，是在最后阶段的

然后接下来注册过滤器到启动类
```
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class ZuulServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( ZuulServerApplication.class, args );
    }
    
    @Bean
    public TokenFilter getZuulFilter(){
        return new TokenFilter();
    }
}
```
## 五、网关请求限流

RateLimiter是Google开源的实现了令牌桶算法的限流工具（速率限制器）。
令牌桶的大体算法逻辑如下：
![](https://user-gold-cdn.xitu.io/2019/7/11/16be04e48339eb87?w=447&h=295&f=png&s=46333)
* a.按特定的速率向令牌桶投放令牌
* b.根据预设的匹配规则先对报文进行分类，不符合匹配规则的报文不需要经过令牌桶的处理，直接发送；
* c.符合匹配规则的报文，则需要令牌桶进行处理。当桶中有足够的令牌则报文可以被继续发送下去，同时令牌桶中的令牌 量按报文的长度做相应的减少；
* d.当令牌桶中的令牌不足时，报文将不能被发送，只有等到桶中生成了新的令牌，报文才可以发送。这就可以限制报文的流量只能是小于等于令牌生成的速度，达到限制流量的目的。
Spring Cloud Zuul RateLimiter结合Zuul对RateLimiter进行了封装，通过实现ZuulFilter提供了服务限流功能.

| 限流粒度/类型 | 说明 |
|------|------------|
| 服务粒度  | 默认配置，当前服务模块的限流控制 |
| 用户粒度  | 针对请求的用户进行限流 |
| ORIGIN粒度  | 用户请求的origin作为粒度控制 |
| 接口URL粒度  | 请求接口的地址作为粒度控制 |

以上粒度自由组合，又可以支持多种情况。
如果还不够，自定义RateLimitKeyGenerator实现。

因为使用到令牌桶算法，作为令牌桶的容器，也支持多个，分别为：
* InMemoryRateLimiter - 使用 ConcurrentHashMap作为数据存储
* ConsulRateLimiter - 使用 Consul 作为数据存储
* RedisRateLimiter - 使用 Redis 作为数据存储
* SpringDataRateLimiter - 使用 数据库 作为数据存储

集成Spring Cloud Zuul RateLimiter的步骤非常简单，大体分为三步：
* 1、引入pom依赖
* 2、修改yml配置文件，加入限流规则
* 3、如果提供的已有限流规则不满足条件，可以自定义(该步可省略)

### 引入pom依赖
```
<dependency>
	<groupId>com.marcosbarbero.cloud</groupId>
    	<artifactId>spring-cloud-zuul-ratelimit</artifactId>
   	<version>2.0.4.RELEASE</version>
</dependency>
```
### 修改yml配置文件，加入限流规则
```
#为所有服务进行限流,3秒内只能请求1次,并且请求时间总数不能超过5秒
zuul:
  ratelimit:
#开启限流 
    enabled: true
#令牌桶的容器方式，使用redis
    repository: REDIS
#默认全部服务开启限流, default-policy
    default-policy:
#限制请求次数
      limit: 1
#限制请求时间
      quota: 5
#多少秒后重置令牌桶
      refresh-interval: 3
```
开启限流后访问服务，3秒内第二次请求会出现如下错误：
```
{
    "code": 429,
    "msg": "Too Many Requests"
}
```
## 六、网关服务级别的熔断处理
针对服务级别层次进行熔断处理，代码如下：
```
/**
 * 定制服务级别的熔断处理
 */
@Component
public class ZuulRoteFallback implements FallbackProvider{

	/**
	 * 指定针对哪个服务进行熔断处理
	 */
	@Override
	public String getRoute() {
		//指定针对哪个服务进行熔断处理
		return "userservice";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		return new ClientHttpResponse() {
			
			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
               			headers.setContentType(MediaType.APPLICATION_JSON);
                		return headers;
			}
			
			/**
			 * 熔断后返回的内容
			 */
			@Override
			public InputStream getBody() throws IOException {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("code", 500);
				jsonObject.put("msg", "用户服务不可用,请稍后重试");
				return new ByteArrayInputStream(jsonObject.toJSONString().getBytes());
			}
			
			@Override
			public String getStatusText() throws IOException {
				return this.getStatusCode().getReasonPhrase();
			}
			
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return HttpStatus.OK;
			}
			
			@Override
			public int getRawStatusCode() throws IOException {
				return this.getStatusCode().value();
			}
			
			@Override
			public void close() {
				
			}
		};
	}

}
```
需要注意的地方有三处：
* 1、实现FallbackProvider接口 重写两个方法。
* 2、getRoute()方法指定需要对哪个服务进行熔断处理，如果配置为\*，则表示对所有服务进行熔断处理。
* 3、ClientHttpResponse中的getBody()指定熔断后提示的错误信息,自己根据系统要求自定义即可。

当userservice不可用时，提示如下信息：
```
{
    "msg": "用户服务不可用,请稍后重试",
    "code": 500
}
```

## 七、利用oauth2.0统一鉴权：
实现资源服务器，[详情点击此处,节点(4、资源服务器的搭建)](https://github.com/yipengcheng001/spring-cloud-scaffolding/tree/master/spring-cloud-auth-server)
