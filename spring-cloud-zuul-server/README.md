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
## 搭建网关服务
搭建最基本的zuul网关
* 1、配置pom.xml，添加spring-cloud-starter-zuul的依赖
* 2、配置application.yml，设置分流操作
* 3、配置启动类，添加@EnableZuulProxy注解，开启动态路由
* 4、配置过滤器，实现ZuulFilter抽象类
* 5、配置过滤器到启动类。
第4和第5步，不是非必须的，而是自定义过滤器的操作

这里就不过多描述
## 动态路由配置与更新
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


## zuul的自定义请求过滤
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

## 利用oauth2.0统一鉴权：
实现资源服务器，继承ResourceServerConfigurerAdapter
```
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter{
	
	@Autowired
	private CustomAccessDeniedHandler customAccessDeniedHandler;
	
	@Autowired
	private AuthExceptionEntryPoint authExceptionEntryPoint;
	
	@Autowired
	private FilterIgnorePropertiesConfig filterIgnorePropertiesConfig;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry hasAuthority = http.authorizeRequests();
        //从yml配置文件中获取不需要认证的url  <1>注意点
        filterIgnorePropertiesConfig.getUrls().forEach(url -> hasAuthority.antMatchers(url).permitAll());
        <2>注意点
        hasAuthority.antMatchers("/**").hasAuthority("USER"); 
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        <3>注意点
        resources.tokenStore(tokenStore());
        <4>注意点
        resources.authenticationEntryPoint(authExceptionEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler);
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
    	// 用作JWT转换器
        JwtAccessTokenConverter converter =  new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource("public.cert");
        String publicKey;
        try {
            publicKey = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //设置公钥
        converter.setVerifierKey(publicKey);
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
}
```
通过如上代码可知：
* 注意点1：将application.yml中配置的url进行单独过滤处理，不需要携带令牌即可访问接口。
* 注意点2：将没有进行yml配置的url，进行统一的拦截认证。
* 注意点3：使用jwt生成令牌，故tokenStore使用JwtTokenStore并进行相应的公钥配置。
* 注意点4：重写token不存在或者token有误、过期时的返回异常结果集。方便做统一返回结果集解析。

针对上述配置演示如下：

/user/user/login与/user/user/register接口没有做认证处理，可直接访问
![](https://user-gold-cdn.xitu.io/2019/6/26/16b927c09da45482?w=1488&h=742&f=png&s=135699)

/user/user/foo 需要进行携带令牌认证后才可以访问：<br>

令牌为空的情况下访问:
![](https://user-gold-cdn.xitu.io/2019/6/26/16b927d3cc95e22b?w=1483&h=461&f=png&s=49485)

令牌为错误的情况下访问：
![](https://user-gold-cdn.xitu.io/2019/6/26/16b927db4574d416?w=1485&h=479&f=png&s=51102)

令牌为正确的情况下访问：
![](https://user-gold-cdn.xitu.io/2019/6/26/16b927e42a97cbd2?w=1486&h=412&f=png&s=48741)
