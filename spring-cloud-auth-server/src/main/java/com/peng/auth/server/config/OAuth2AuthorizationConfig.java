package com.peng.auth.server.config;

import java.util.Arrays;
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
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;



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
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
                Arrays.asList(tokenEnhancer(), jwtTokenEnhancer()));//配置了JWT Token的非对称加密来进行签名

        endpoints.approvalStore(approvalStore())//配置了使用JDBC数据库方式来保存用户的授权批准记录
                .authorizationCodeServices(authorizationCodeServices())
                .tokenStore(tokenStore())	//配置我们的Token存放方式不是内存方式、不是数据库方式、不是Redis方式而是JWT方式
                .tokenEnhancer(tokenEnhancerChain)//配置了一个自定义的Token增强器，把更多信息放入Token中
                .authenticationManager(authenticationManager);
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
