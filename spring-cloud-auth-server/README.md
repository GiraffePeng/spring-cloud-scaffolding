# 授权服务器
## 1、oauth2介绍
OAuth2.0是一套授权体系的开放标准，定义了四大角色：

* 资源拥有者，也就是用户，由用于授予三方应用权限
* 客户端，也就是三方应用程序，在访问用户资源之前需要用户授权
* 资源提供者，或者说资源服务器，提供资源，需要实现Token和ClientID的校验，以及做好相应的权限控制
* 授权服务器，验证用户身份，为客户端颁发Token，并且维护管理ClientID、Token以及用户

其中后三项都可以是独立的程序。OAuth2.0标准同时定义了四种授权模式，这里介绍常用的三种(授权码、密码模式、客户端模式)

1、不管是哪种模式，通用流程如下：

* 三方网站（或者说客户端）需要先向授权服务器去申请一套接入的ClientID+ClientSecret
* 用任意一种模式拿到访问Token（流程见下）
* 拿着访问Token去资源服务器请求资源
* 资源服务器根据Token查询到Token对应的权限进行权限控制

2、授权码模式，最标准最安全的模式，适合和外部交互，流程是：

* 三方网站客户端转到授权服务器，上送ClientID，授权范围Scope、重定向地址RedirectUri等信息
* 用户在授权服务器进行登录并且进行授权批准（授权批准这步可以配置为自动完成）
* 授权完成后重定向回到之前客户端提供的重定向地址，附上授权码
* 三方网站服务端通过授权码+ClientID+ClientSecret去授权服务器换取Token（Token含访问Token和刷新Token，访问Token过去后用刷新Token去获得新的访问Token）
* 你可能会问这个模式为什么这么复杂，为什么安全呢？因为我们不会对外暴露ClientSecret，不会对外暴露访问Token，使用授权码换取Token的过程是服务端进行，客户端拿到的只是一次性的授权码

3、密码凭证模式，适合内部系统之间使用的模式（客户端是自己人，客户端需要拿到用户帐号密码），流程是：

* 用户提供帐号密码给客户端
* 客户端凭着用户的帐号密码，以及客户端自己的ClientID+ClientSecret去授权服务器换取Token

4、客户端模式，适合内部服务端之间使用的模式：

* 和用户没有关系，不是基于用户的授权
* 客户端凭着自己的ClientID+ClientSecret去授权服务器换取Token

## 2、JWT
通过 JWT 配合 Spring Security OAuth2 使用的方式，可以避免每次请求都远程调度认证授权服务。资源服务器只需要从授权服务器 验证一次，返回 JWT。返回的 JWT 包含了 用户 的所有信息，包括 权限信息
### 2.1、什么是JWT
JSON Web Token（JWT）是一种开放的标准（RFC 7519），JWT 定义了一种 紧凑 且 自包含 的标准，旨在将各个主体的信息包装为 JSON 对象。主体信息 是通过 数字签名 进行 加密 和 验证 的。经常使用 HMAC 算法或 RSA（公钥/私钥 的 非对称性加密）算法对 JWT 进行签名，安全性很高。

* 紧凑型：数据体积小，可通过 POST 请求参数 或 HTTP 请求头 发送。
* 自包含：JWT 包含了主体的所有信息，避免了 每个请求 都需要向 Uaa 服务验证身份，降低了 服务器的负载。
### 2.2、JWT结构
JWT 的结构由三部分组成：Header（头）、Payload（有效负荷）和 Signature（签名）。因此 JWT 通常的格式是 xxxxx.yyyyy.zzzzz

#### 2.2.1、Header
Header通常是由两部分组成：令牌的类型（即 JWT）和使用的 算法类型，如 HMAC、SHA256 和 RSA。例如:
```
{
    "typ": "JWT",
    "alg": "HS256"
}
```
将 Header 用 Base64 编码作为 JWT的第一部分，不建议在 JWT 的 Header 中放置 敏感信息。
#### 2.2.2、Payload
第二部分 Payload 是 JWT 的 主体内容部分，它包含 声明 信息。声明是关于 用户 和 其他数据 的声明。

声明有三种类型: registered、public 和 private。
* Registered claimsJWT 提供了一组 预定义 的声明，它们不是 强制的，但是推荐使用。JWT 指定 七个默认 字段供选择：

| 注册声明 | 字段含义   |
|:-----:   | :--------- |
|iss|发行人|
|exp|到期时间|
|sub|主题|
|aud|用户|
|nbf|在此之前不可用|
|iat|发布时间|
|jti|用于标识JWT的ID|
* Public claims：可以随意定义
* Private claims：用于在同意使用它们的各方之间共享信息，并且不是注册的或公开的声明。
下面是 Payload 部分的一个示例：
```
{
    "sub": "123456789",
    "name": "John Doe",
    "admin": true
}
```
将 Payload 用 Base64 编码作为 JWT 的 第二部分，不建议在 JWT 的 Payload 中放置 敏感信息。

#### 2.2.3、Signature
要创建签名部分，需要利用 秘钥 对 Base64 编码后的 Header 和 Payload 进行 加密，加密算法的公式如下：
```
HMACSHA256(
    base64UrlEncode(header) + '.' +
    base64UrlEncode(payload),
    secret
)
```
签名可以用于验证消息在传递过程中有没有被更改。对于使用私钥签名的token，它还可以验证JWT的发送方是否为它所称的发送方。

### 2.3、JWT的工作方式
客户端 获取 JWT 后，对于以后的 每次请求，都不需要再通过 授权服务 来判断该请求的 用户 以及该 用户的权限。在微服务系统中，可以利用 JWT 实现 单点登录。认证流程图如下：
![](https://user-gold-cdn.xitu.io/2019/7/24/16c23275831eb16b?w=508&h=330&f=png&s=31188)

## 3、授权服务器的搭建
父级pom.xml这里省略，基于父级工程spring-cloud-scaffolding即可。
### 3.1、授权服务器引入依赖:
```
<dependencies>
    <!-- oauth2.0依赖 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
    <!-- web相关依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!--reids -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!--jpa数据访问 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <!--mysql连接 -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <!-- 断路器-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
    <!-- 健康监控-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!-- 链路跟踪-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zipkin</artifactId>
    </dependency>
    <!-- 链路跟踪-->
    <dependency>
        <groupId>com.github.gavlyukovskiy</groupId>
        <artifactId>p6spy-spring-boot-starter</artifactId>
        <version>1.4.3</version>
    </dependency>
    <!-- 注册中心注册-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```
### 3.2、建立application.yml文件：
```
spring:
  application:
    name: auth-service
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ceshi?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8
    username: huaxin
    password: Koreyoshih527
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  redis:
    host: localhost
    database: 0
    port: 6379

hystrix:
    command:
        default:
            execution:
                isolation:
                    thread:
                        timeout-in-milliseconds: 3000

server:
  port: 8599
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8865/eureka/
```
会使用到mysql数据库，授权服务器的端口是8599。
### 3.3、建立表
因为授权服务器的客户端信息以及用户信息要放入数据库中，我们需要初始化一些表：
* user_auth表用于管理端oauth2的用户信息记录。
* member_auth表用于移动端oauth2的会员信息记录。
* role_auth表，存放了用户的权限信息
* oauth_approvals授权批准表，存放了用户授权第三方服务器的批准情况
* oauth_client_details，客户端信息表，存放客户端的ID、密码、权限、允许访问的资源服务器ID以及允许使用的授权模式等信息
* oauth_code授权码表，存放了授权码。

表结构如下：
```
CREATE TABLE `user_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表';

CREATE TABLE `member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8 COMMENT='会员表';


CREATE TABLE `role_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authority` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`authority`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8 COMMENT='用户角色表';

CREATE TABLE `oauth_approvals` (
  `userId` varchar(256) DEFAULT NULL,
  `clientId` varchar(256) DEFAULT NULL,
  `partnerKey` varchar(32) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `expiresAt` datetime DEFAULT NULL,
  `lastModifiedAt` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(64) NOT NULL,
  `resource_ids` varchar(255) DEFAULT NULL,
  `client_secret` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `authorized_grant_types` varchar(255) DEFAULT NULL,
  `web_server_redirect_uri` varchar(255) DEFAULT NULL,
  `authorities` varchar(255) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(1000) DEFAULT NULL,
  `autoapprove` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `oauth_code` (
  `code` varchar(255) DEFAULT NULL,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
因为客户端存入数据库中，需要初始化下客户端的信息
```
INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES ('userservice', '', '$2a$10$ZRyWnA9PY8Wn.LPN0DtxKer4NF/COK7asCXOAemZSazliGhlIBVk.', 'service', 'password,client_credentials,custom_phone_pwd,custom_phone_sms,refresh_token', NULL, NULL, '86400', '2592000', NULL, 'true');

```

没有在数据库中创建相应的表来存放访问令牌、刷新令牌，这是因为令牌信息会使用JWT来传输，不会存放到数据库中。
### 3.4、配置授权服务器
```
package com.peng.auth.server.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import com.peng.auth.server.mobile.CustomUserDetailsService;
import com.peng.auth.server.mobile.PhonePasswordCustomTokenGranter;
import com.peng.auth.server.mobile.PhoneSmsCustomTokenGranter;





/**
 * 授权服务器（authorization server）：成功验证资源拥有者并获取授权之后，授权服务器颁发授权令牌（Access Token）给客户端。
[/oauth/authorize] 用于授权码模式下获取code
[/oauth/token]  用于授权，获取token
[/oauth/check_token]  用于校验token的有效性
[/oauth/confirm_access]  用于用户确认授权提交
[/oauth/token_key] 提供公有密匙的端点，如果你使用JWT令牌的话
[/oauth/error] 授权服务错误信息
 */
@EnableAuthorizationServer //通过注解@EnableAuthorizationServer来开启授权服务器
@Configuration
public class OAuth2AuthorizationConfig extends AuthorizationServerConfigurerAdapter{
	
    @Autowired
    private DataSource dataSource;
	@Autowired
	@Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    public CustomUserDetailsService customUserDetailsService;
    
    /**
     * 配置了使用数据库来维护客户端信息，下面注释的为将客户端信息存储在内存中，通过配置直接写死在这里(生产环境还是推荐使用数据库来存储)
     * 对于实际的应用我们一般都会用数据库来维护这个信息。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	// 将客户端的信息存储在数据库中
    	clients.jdbc(dataSource);
    	// 将客户端的信息存储在内存中
        /*clients.inMemory()
                // 配置一个客户端
                .withClient("userservice")
                .secret(new BCryptPasswordEncoder().encode("123456"))
                // 配置客户端的域
                .scopes("service")
                 // 配置验证类型为refresh_token和password
                .authorizedGrantTypes("refresh_token", "password","client_credentials")
                // 配置token的过期时间为1h
                .accessTokenValiditySeconds(3600 * 1000);*/
    }
    
    /**
     * 配置我们的Token存放方式不是内存方式、不是数据库方式、不是Redis方式而是JWT方式，
     * JWT是Json Web Token缩写也就是使用JSON数据格式包装的Token，由.句号把整个JWT分隔为头、数据体、签名三部分，JWT保存Token虽然易于使用但是不是那么安全，一般用于内部，并且需要走HTTPS+配置比较短的失效时间
	 * 配置了JWT Token的非对称加密来进行签名
	 * 配置了一个自定义的Token增强器，把更多信息放入Token中
	 * 配置了使用JDBC数据库方式来保存用户的授权批准记录
	 * 配置自定义grant，满足移动端的授权条件
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
                Arrays.asList(tokenEnhancer(), jwtTokenEnhancer()));//配置了JWT Token的非对称加密来进行签名

        List<TokenGranter> tokenGranters = getTokenGranters(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory());
        tokenGranters.add(endpoints.getTokenGranter());
        
        endpoints.setClientDetailsService(clientDetailsService()); //配置从JDBC中获取客户端配置信息
        endpoints.approvalStore(approvalStore())//配置了使用JDBC数据库方式来保存用户的授权批准记录
                .authorizationCodeServices(authorizationCodeServices())
                .tokenStore(tokenStore())	//配置我们的Token存放方式不是内存方式、不是数据库方式、不是Redis方式而是JWT方式
                .tokenEnhancer(tokenEnhancerChain)//配置了一个自定义的Token增强器，把更多信息放入Token中
                .authenticationManager(authenticationManager)
                .tokenGranter(new CompositeTokenGranter(tokenGranters)); //配置自定义的granter  这里有通过手机号密码的形式以及手机号短信验证码的形式  对应grant_type为custom_phone_pwd custom_phone_sms
    }
    
    private List<TokenGranter> getTokenGranters(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        return new ArrayList<TokenGranter>(Arrays.asList(
                new PhoneSmsCustomTokenGranter(tokenServices, clientDetailsService, requestFactory, customUserDetailsService),
                new PhonePasswordCustomTokenGranter(tokenServices, clientDetailsService, requestFactory, customUserDetailsService)
        ));
    }
    
    @Bean
    public ClientDetailsService clientDetailsService() {
        return new JdbcClientDetailsService(dataSource);
    }
    
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }
    
    @Bean
    public JdbcApprovalStore approvalStore() {
        return new JdbcApprovalStore(dataSource);
    }
    
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }
    
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }
    
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.tokenKeyAccess("permitAll()")         
                   .checkTokenAccess("isAuthenticated()") 
                   .allowFormAuthenticationForClients();
    }
    
    
    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        // 配置jks文件  该文件可以使用keytool生成
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("test-jwt.jks"), "test123".toCharArray());
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("test-jwt"));
        return converter;
    }
    
}

```
解释都在上述代码的注释中，这里不多阐述。
大体步骤：
* 1、继承AuthorizationServerConfigurerAdapter，重写其中的三个方法。
* 2、通过注解@EnableAuthorizationServer来开启授权服务器
* 3、重写第一个方法，声明客户端信息
* 4、重写第二个方法，声明token的存储方式以及一些自定义信息
* 5、重写第三个方法，声明验证Token的访问权限以及是否允许表单提交等等
* 6、如果使用jwt传输token，还需要声明jks文件的相关名称以及密码。

自定义token负荷部分的信息,来丰富token的内容：CustomTokenEnhancer类，
```
/**
 * 这段代码非常简单，就是把用户信息以userDetails这个Key存放到Token中去（如果授权模式是客户端模式这段代码无效，因为和用户没关系）
 * 这是一个常见需求，默认情况下Token中只会有用户名这样的基本信息，
 * 我们往往需要把有关用户的更多信息返回给客户端（在实际应用中你可能会从数据库或外部服务查询更多的用户信息加入到JWT Token中去），
 * 这个时候就可以自定义增强器来丰富Token的内容。
 */
public class CustomTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Authentication userAuthentication = authentication.getUserAuthentication();
        if (userAuthentication != null) {
            Object principal = authentication.getUserAuthentication().getPrincipal();
            Map<String, Object> additionalInfo = new HashMap<>();
            additionalInfo.put("userDetails", principal);
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        }
        return accessToken;
    }
}
```
针对移动端常用的手机号+密码登陆或者手机号+短信验证码登陆的场景，自定义grant_type类型，加入custom_phone_pwd和custom_phone_sms类型。

创建用户细节服务
```
package com.peng.auth.server.mobile;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.stereotype.Service;

import com.peng.auth.server.repository.Member;
import com.peng.auth.server.repository.MemberRepository;



//创建用户细节服务
@Service
public class CustomUserDetailsService {
	
	@Autowired
	private MemberRepository memberRepository;
	
	public UserDetails loadUserByPhoneAndPassword(String phone, String password) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            throw new InvalidGrantException("无效的手机号或密码");
        }
        Member member = memberRepository.findByUsername(phone);
        member.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("USER"));
        // 判断成功后返回用户细节
        return member;
    }

    public UserDetails loadUserByPhoneAndSmsCode(String phone, String smsCode) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(smsCode)) {
            throw new InvalidGrantException("无效的手机号或短信验证码");
        }
        Member member = memberRepository.findByUsername(phone);
        //判断短信是否正确
        member.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("USER"));
        return member;
    }
}

```
身份验证令牌
```
package com.peng.auth.server.mobile;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

//身份验证令牌
public class AuthenticationToken extends AbstractAuthenticationToken{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Object principal;
	
	private Object credentials;

	public AuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {
		super(authorities);
        this.principal = principal;
        this.credentials = credentials;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

}

```
创建自定义抽象令牌授予者
```
package com.peng.auth.server.mobile;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

//创建自定义抽象令牌授予者
public abstract class AbstractCustomTokenGranter extends AbstractTokenGranter {

	protected AbstractCustomTokenGranter(AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
	}

	@Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap(tokenRequest.getRequestParameters());
        UserDetails details = getUserDetails(parameters);
        if (details == null) {
            throw new InvalidGrantException("无法获取用户信息");
        }
        AuthenticationToken authentication = new AuthenticationToken(details.getAuthorities(),parameters, details);
        authentication.setAuthenticated(true);
        authentication.setDetails(details);
        OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, authentication);
    }

    protected abstract UserDetails getUserDetails(Map<String, String> parameters);
}

```
手机号密码登录令牌授予者
```
package com.peng.auth.server.mobile;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;


//手机号密码登录令牌授予者
public class PhonePasswordCustomTokenGranter extends AbstractCustomTokenGranter {

    private CustomUserDetailsService userDetailsService;

    public PhonePasswordCustomTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, CustomUserDetailsService userDetailsService) {
        super(tokenServices, clientDetailsService, requestFactory,"custom_phone_pwd");
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected UserDetails getUserDetails(Map<String, String> parameters) {
        String phone = parameters.get("phone");
        String password = parameters.get("password");
        parameters.remove("password");
        return userDetailsService.loadUserByPhoneAndPassword(phone, password);
    }
}

```
短信验证码登录令牌授予者
```
package com.peng.auth.server.mobile;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;


public class PhoneSmsCustomTokenGranter extends AbstractCustomTokenGranter{

    private CustomUserDetailsService userDetailsService;

    public PhoneSmsCustomTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, CustomUserDetailsService userDetailsService) {
        super(tokenServices, clientDetailsService, requestFactory,"custom_phone_sms");
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected UserDetails getUserDetails(Map<String, String> parameters) {
        String phone = parameters.get("phone");
        String smsCode = parameters.get("sms_code");
        return userDetailsService.loadUserByPhoneAndSmsCode(phone, smsCode);
    }
}
```

### 3.5 生成密钥
我们需要使用keytool工具生成密钥，把密钥文件jks保存到目录下，然后还要导出一个公钥留作以后使用。
#### 3.5.1 安装keytool工具
这里使用openSSL来生成密钥，下载地址：[openSSL](http://slproweb.com/products/Win32OpenSSL.html),然后手动把安装的 openssl.exe 所在目录配置到环境变量。
#### 3.5.2 生成密钥
jks 文件的生成需要使用 Java keytool 工具，保证 Java 环境变量没问题，输入命令如下：
```
keytool   -genkeypair -alias test-jwt 
          -validity 3650 
          -keyalg RSA 
          -dname "CN=jwt,OU=jtw,O=jwt,L=zurich,S=zurich, C=CH" 
          -keypass test123 
          -keystore test-jwt.jks 
          -storepass test123

```
其中，-alias 选项为 别名，-keyalg 为 加密算法，-keypass 和 -storepass 为 密码选项，-keystore 为 jks 的 文件名称，-validity 为配置 jks 文件 过期时间（单位：天）。
生成的 jks 文件作为 私钥，只允许 授权服务 所持有，用作 加密生成 JWT。把生成的 jks 文件放到 授权工程的 src/main/resource 目录下即可。
#### 3.5.3 生成公钥
对于 资源服务，需要使用 jks的公钥对JWT进行解密。获取 jks文件的 公钥 的命令如下：
```
keytool   -list -rfc 
          --keystore test-jwt.jks | openssl x509 
          -inform pem 
          -pubkey
```
输入密码 test123 后，显示的信息很多，只需要提取 PUBLIC KEY，即如下所示:
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl+jT1KAbFsQQf9eAwi4r
1O7UEHIjbMTmC9Llm9whSrkyiyOMHYKKePCWqCOMRkT3ugOSj9dJXw/8PsQpuMgD
XROQjzE0tVvAt5U7M1jv1FZpVy7eikYvXM7CxtihpctztGQp20TpSxPlkJw8wCBS
nl2CzMQmvETBGfUc09rwTc8f4oQfL8jwz+aGK69tlt47GMwcNFyOJPbD8CX67n+P
+/pJzysoov6f9msIhWO/+cEHJgejaRf4RmlI2bgc+o00u4GI+p8lheOlBsnNCDGM
YcaZdz6UyaQelT9pLuBlE638UefhLfOh+rw4QPgG8XAhAuaS/4bEBqy79FHA/ggI
oQIDAQAB
-----END PUBLIC KEY-----
```
注意这里的-----BEGIN PUBLIC KEY-----以及-----END PUBLIC KEY-----也需要提取出来。

新建一个 public.cert 文件，将上面的公钥信息复制到 public.cert 文件中并保存。并将文件放到资源服务(本项目的为spring-cloud-zuul-server)的src/main/resources 目录下。至此授权服务器的核心配置已经完成。
### 3.6 安全配置
再来实现一下安全方面的配置,用于保护 token 发放和验证的资源接口。：
```
/**
 * 由于 auth-service 需要对外暴露检查 Token 的 API 接口，所以 auth-service 
 * 其实也是一个 资源服务，
 * 需要在 auth-service 中引入 Spring Security，并完成相关配置，从而对 auth-service 的 资源 进行保护。
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
    private UserServiceDetail userServiceDetail;
	
	 @Override
	 protected void configure(HttpSecurity http) throws Exception {
		 http.csrf().disable() //关闭CSRF
         .authorizeRequests()
         .antMatchers("/oauth/authorize")//开放/oauth/authorize路径的匿名访问，后者用于换授权码，这个端点访问的时候在登录之前
         .permitAll()
         .anyRequest() //其他所有路径访问，进行验证
         .authenticated()
     .and()
         .httpBasic();
	 }
 
	 @Bean
	 public BCryptPasswordEncoder passwordEncoder(){
		 return new BCryptPasswordEncoder();
	 }

	 //我们把用户存在了数据库中希望配置JDBC的方式
	 //此外，我们还配置了使用BCryptPasswordEncoder加密来保存用户的密码（生产环境的用户密码肯定不能是明文保存）
	 @Override
	 protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		 auth.userDetailsService(userServiceDetail).passwordEncoder(passwordEncoder());
	 }
	 
	 @Override
	 public @Bean AuthenticationManager authenticationManagerBean() throws Exception {
	     return super.authenticationManagerBean();
	 }
	
}

```
这里通过实现UserDetailsService接口，来重写用户信息的获取方式
```
@Service
public class UserServiceDetail implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		List<Role> roles = roleRepository.findByUserId(user.getId());
		user.setAuthorities(roles);
		return user;
	}
}
```
配置表的关系映射类 User，需要实现 UserDetails 接口：
```
@Entity
@Table(name = "user_auth")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails,Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String password;
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Role> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    	return authorities;
    }
	
    public void setAuthorities(List<Role> authorities) {
    	this.authorities = authorities;
    }

    public Long getId() {
    	return id;
    }

    @Override
    public String getPassword() {
    	return password;
    }
    
    @Override
    public String getUsername() {
    	return username;
    }

    @Override
    public boolean isAccountNonExpired() {
    	return true;
    }

    @Override
    public boolean isAccountNonLocked() {
    	return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
    	return true;
    }
	

    public void setId(Long id) {
    	this.id = id;
    }

    public void setUsername(String username) {
    	this.username = username;
    }

    public void setPassword(String password) {
    	this.password = password;
    }

    @Override
    public boolean isEnabled() {
    	return true;
    }

}
```
配置表的关系映射类 Role，需要实现 GrantedAuthority 接口：
```
@Entity
@Table(name = "role_auth")
public class Role implements GrantedAuthority{
	

    private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String authority;
    
    @Column(nullable = false)
    private Long userId;
    

    public void setId(Long id) {
    	this.id = id;
    }

    public void setAuthority(String authority) {
    	this.authority = authority;
    }

    public void setUserId(Long userId) {
    	this.userId = userId;
    }

    @Override
    public String getAuthority() {
    	return authority;
    }
	
    @Override
    public String toString() {
    	return authority;
    }

}
```
使用JPA查询user_auth以及role_auth表的数据
```
@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	User findByUsername(String username);

}
```
```
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

	List<Role> findByUserId(Long userId);

}
```
### 3.7 启动类配置
最后配置该服务的启动类即可
```
@SpringCloudApplication
@EnableHystrix
@Configuration
public class AuthServerApplicaiton {

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplicaiton.class, args);
	}
}
```

至此授权服务器配置完成，大体步骤如下：
* 引入oauth2.0依赖。
* 配置application.yml。
* 初始化数据库中的表。
* 继承AuthorizationServerConfigurerAdapter来实现授权服务的配置。
* 引入Spring Security，并完成相关配置，对资源进行保护。
* 生成密钥以及公钥，并放在相应的资源文件夹下。
* 启动类的配置。

## 4、资源服务器的搭建
本项目结构中使用了网关zuul，故将网关服务当做资源服务器来使用，好处在于如果有多个业务模块服务，不用在每个业务模块上进行资源服务的配置，统一在网关处理即可。

### 4.1 配置资源服务
```
@Configuration
@EnableResourceServer//@EnableResourceServer启用资源服务器
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
        //从yml配置文件中获取不需要认证的url(即匿名能访问的url)
        filterIgnorePropertiesConfig.getUrls().forEach(url -> hasAuthority.antMatchers(url).permitAll());
        hasAuthority.antMatchers("/**").hasAuthority("USER");//除上述url，其他url必须携带权限有USER的token才可以访问
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore());//声明了资源服务器的TokenStore是JWT以及公钥
        
        resources.authenticationEntryPoint(authExceptionEntryPoint)//无效token 或token不存在异常类重写
                .accessDeniedHandler(customAccessDeniedHandler);//权限不足异常类重写
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
    	// 用作JWT转换器  获取resource目录下的public.cert文件
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
解释同样在代码的注解中，大体步骤如下：
* @EnableResourceServer启用资源服务器
* 继承ResourceServerConfigurerAdapter类，重写两个方法
* 第一个方法用于声明资源服务器的哪些资源需要被保护，哪些可以匿名访问。
* 第二个方法用于声明token的类型以及存储地方和可以重写一些oauth2.0报出的异常，方便进行格式统一的数据返回格式。

### 4.2 配置公钥
将3.5.3节点中生成的公钥放在资源服务(spring-cloud-zuul-server)下的/src/main/resources即可。

### 4.3 配置yml文件
同样在的/src/main/resources目录下建立 application.yml文件。内容如下：
```
logging:
  level:
    org.springframework: DEBUG
server:
  port: 8866

ribbon:
  ReadTimeout: 3000
  ConnectTimeout: 2000

spring:
  application:
    name: zuulserver
  main:
    allow-bean-definition-overriding: true
  zipkin:
      base-url: http://localhost:9411
  sleuth:
    feign:
      enabled: true
    sampler:
      probability: 1.0
  redis:
    host: localhost
    database: 0
    port: 6379
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8865/eureka/
    registry-fetch-interval-seconds: 5

zuul:
# 集成限流，打开注释 为所有服务进行限流,3秒内只能请求1次,并且请求时间总数不能超过5秒
#  ratelimit:
#    enabled: true
#    repository: REDIS
#    default-policy:
#      limit: 1
#      quota: 5
#      refresh-interval: 3
  host:
    connect-timeout-millis: 10000
    socket-timeout-millis: 60000
  routes:
    invest:
      path: /invest/**
      serviceId: investservice
    user:
      path: /user/**
      serviceId: userservice
    project:
      path: /project/**
      serviceId: projectservice
    auth:
      path: /auth/**
      serviceId: auth-service
  add-proxy-headers: true
  sensitive-headers:
  retryable: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude: env
  endpoint:
    health:
      show-details: always
feign:
  hystrix:
    enabled: true
hystrix:
  command:
    default:
      execution:
        timeout:
          enabled: true
        isolation:
          thread:
            timeoutInMilliseconds: 10000
#---------------此处以下为资源服务器的配置
security:
  basic:
    enabled: false
  oauth2:
    client:
      access-token-uri: http://localhost:8866/auth/oauth/token
      user-authorization-uri: http://localhost:8866/auth/oauth/authorize
      client-id: userservice
    resource:
      user-info-uri: http://localhost:8866/auth/user
      prefer-token-info: false
      jwt:
        key-value: test123
        key-uri: http://localhost:8599/oauth/token_key
ignore:
  urls[0]: /auth/**
  urls[1]: /user/user/register
  urls[2]: /user/user/login
  urls[3]: /swagger-resources/**
  urls[4]: /swagger-ui.html
  urls[5]: /*/v2/api-docs
  urls[6]: /swagger/api-docs
  urls[7]: /webjars/**
  urls[8]: /zuulRoute/refresh
```
### 4.4 将可以匿名访问的url进行配置在yml中(拓展)
获取需要匿名访问的url类：
```
@Data
@Configuration
@RefreshScope
@ConditionalOnExpression("!'${ignore}'.isEmpty()")
@ConfigurationProperties(prefix = "ignore")
public class FilterIgnorePropertiesConfig {
    private List<String> urls = new ArrayList<>();
}
```
### 4.5 自定义异常格式(拓展)
```
/**
 * 无效token 或token不存在异常类重写
 */
@Component
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws ServletException {
        Throwable cause = authException.getCause();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "401");
        jsonObject.put("data", "");
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            if(cause instanceof InvalidTokenException) {
            	jsonObject.put("msg", "token格式非法或失效");
                response.getWriter().write(jsonObject.toJSONString());
            }else{
            	jsonObject.put("msg", "token缺失");
                response.getWriter().write(jsonObject.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

```
/**
 * 权限不足异常类重写
 */
@Component("customAccessDeniedHandler")
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
        	JSONObject jsonObject = new JSONObject();
        	jsonObject.put("data", null);
        	jsonObject.put("code", "401");
        	jsonObject.put("msg", "权限不足");
            response.getWriter().write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```


## 5、演示
启动spring-cloud-eureka-server、spring-cloud-zuul-server、spring-cloud-auth-server、spring-cloud-userservice-server。

/user/user/login与/user/user/register接口没有做认证处理，可直接访问 

![](https://user-gold-cdn.xitu.io/2019/7/25/16c2721bc9b7b004?w=1488&h=742&f=png&s=135699)

/user/user/foo 需要进行携带令牌认证后才可以访问：

令牌为空的情况下访问: 

![](https://user-gold-cdn.xitu.io/2019/7/25/16c2722046d383a6?w=1483&h=461&f=png&s=49485)

令牌为错误的情况下访问： 

![](https://user-gold-cdn.xitu.io/2019/7/25/16c272232ff86117?w=1485&h=479&f=png&s=51102)

令牌为正确的情况下访问： 

![](https://user-gold-cdn.xitu.io/2019/7/25/16c2722745e51718?w=1486&h=412&f=png&s=48741)
