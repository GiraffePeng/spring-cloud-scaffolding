# spring cloud zuul 集成 swagger
介入该项目存在zuul网关服务，需求为通过zuul统一查看各个服务的Result API文档，不再需要访问单独业务服务的地址来查阅API文档。

## 引入依赖
建议引入的swagger版本使用比较高的，此处version使用2.9.2，将该依赖引入父级pom工程中。
```
		<!-- Swagger核心包 start -->
 		<dependency>
   			<groupId>io.springfox</groupId>
   			<artifactId>springfox-swagger2</artifactId>
   			<version>2.9.2</version>
 		</dependency>
 		<dependency>
    		<groupId>io.springfox</groupId>
    		<artifactId>springfox-swagger-ui</artifactId>
    		<version>2.9.2</version>
 		</dependency>
		<!-- Swagger核心包 end -->
```

## 添加认证过滤url
由于用zuul作为资源服务器，如果不将swagger的页面和请求路径进行过滤，将无法正常访问API文档。添加代码如下：

zuul-server中application.yml文件
```
ignore:
  urls[3]: /swagger-resources/**
  urls[4]: /swagger-ui.html
  urls[5]: /*/v2/api-docs
  urls[6]: /swagger/api-docs
  urls[7]: /webjars/**
```
然后在ResourceServerConfig类中统一循环过滤,详见：[zuul网关服务](https://github.com/yipengcheng001/spring-cloud-scaffolding/blob/master/spring-cloud-zuul-server/README.md)
```
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry hasAuthority = http.authorizeRequests();
        //从yml配置文件中获取不需要认证的url
        filterIgnorePropertiesConfig.getUrls().forEach(url -> hasAuthority.antMatchers(url).permitAll());
        hasAuthority.antMatchers("/**").hasAuthority("USER");
    }
```

## 构建swagger的服务下拉列表
```
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class ZuulServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( ZuulServerApplication.class, args );
    }
    
    @Component
    @Primary
    class DocumentationConfig implements SwaggerResourcesProvider {
    
    //此处通过根据zuul的路由配置，进行添加swagger的服务下拉列表
		  @Override
		  public List<SwaggerResource> get() {
			  List<SwaggerResource> resources = new ArrayList<SwaggerResource>();
			  resources.add(swaggerResource("用户服务","/user/v2/api-docs","1.0"));
			  resources.add(swaggerResource("项目服务","/project/v2/api-docs","1.0"));
			  return resources;
		  }

		  private SwaggerResource swaggerResource(String name, String location, String version) {
			  SwaggerResource swaggerResource = new SwaggerResource();
			  swaggerResource.setName(name);
			  swaggerResource.setLocation(location);
			  swaggerResource.setSwaggerVersion(version);
			  return swaggerResource;
		  }
	  }
}

```

## 构建swagger在zuul的统一界面
```
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
    	ParameterBuilder ticketPar = new ParameterBuilder();
      
    	List<Parameter> pars = new ArrayList<>();
      
      Parameter build = ticketPar.name("Authorization").description("jwt的Token")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build(); //header中的Authorization参数非必填，传空也可以
                
      pars.add(build);  
      return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()).globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("脚手架")
                .description("spring cloud 脚手架")
                .termsOfServiceUrl("")
                .version("1.0")
                .build();
    }

    @Bean
    UiConfiguration uiConfig() {
        return new UiConfiguration(null, "list", "alpha", "schema",
                UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS, false, true, 60000L);
    }
}
```
至此zuul项目的改造完成，接下来是针对对应服务的改造，此处只提及userservice-server的改造，其中项目步骤是一致的。
## 改造userservice-server 引入依赖
因为依赖是引入在了父级pom工程中，此处无需再次依赖

## 改造userservice-server 配置swagger的config
```
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Bean
    public Docket createRestApi() {
    //加入swagger中的header固定头部信息(Authorization),为了传递jwt的token令牌使用。
		ParameterBuilder ticketPar = new ParameterBuilder();
    
    List<Parameter> pars = new ArrayList<>();
    Parameter build = ticketPar.name("Authorization").description("jwt的Token")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build(); //header中的Authorization参数非必填，传空也可以
                
    pars.add(build);  
		return new Docket(DocumentationType.SWAGGER_2)
				  .apiInfo(apiInfo()) // 配置说明
			      .select() // 选择那些路径和 api 会生成 document
			      .apis(RequestHandlerSelectors.basePackage("com.peng"))
	              .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
	              .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
	              .paths(PathSelectors.any())
			      .build().globalOperationParameters(pars); // 创建
			      
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("用户服务 Swagger API").description("spring cloud 脚手架")
                .termsOfServiceUrl("").version("1.0").build();
    }
}
```

## 改造userservice-server 在controller上加入swagger介绍注解
```
@RestController
@RequestMapping("/user")
@Api(tags = "用户平台接口")  //swagger类注解
public class UserServiceController implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserServiceDetail userServiceDetail;
    @Autowired
    RedissonClient redissonClient;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册接口", httpMethod = "POST") //swagger方法注解
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", defaultValue = "请输入用户名"),
        @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", defaultValue = "请输入密码")
    })
    public com.peng.userservice.server.User postUser(@RequestParam("username") String username,
                         @RequestParam("password") String password){
       return userServiceDetail.insertUser(username, password);
    }
}
```

## swagger的大体注解如下：
| 作用范围 | API | 使用位置 |
|:-: | :----- | :----- |
|对象属性|@ApiModelProperty|用在出入参数对象的字段上|
|协议集描述|@Api|用于controller类上|
|协议描述|@ApiOperation|用在controller的方法上|
|Response集|@ApiResponses|用在controller的方法上|
|Response|@ApiResponse|用在 @ApiResponses里边|
|非对象参数集|@ApiImplicitParams|用在controller的方法上|
|非对象参数描述|@ApiImplicitParam|用在@ApiImplicitParams的方法里边|
|描述返回对象的意义|@ApiModel|用在返回对象类上|

如图所示:
![](https://user-gold-cdn.xitu.io/2019/6/26/16b9316b3990ec17?w=1301&h=837&f=png&s=53721)
