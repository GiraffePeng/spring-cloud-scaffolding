# 网关服务
* 作为所有接口请求入口，负责进行请求的流量转发。
* 同时作为资源认证服务，将需要带有令牌访问的接口进行认证，如果不符合要求，进行返回错误信息。

资源服务配置如下：
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
