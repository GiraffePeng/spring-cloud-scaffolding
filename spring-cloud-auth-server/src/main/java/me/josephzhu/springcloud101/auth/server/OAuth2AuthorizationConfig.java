package me.josephzhu.springcloud101.auth.server;

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
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;


/**
 * 授权服务器（authorization server）：成功验证资源拥有者并获取授权之后，授权服务器颁发授权令牌（Access Token）给客户端。
[/oauth/authorize]
[/oauth/token]
[/oauth/check_token]
[/oauth/confirm_access]
[/oauth/token_key]
[/oauth/error]
 */
@EnableAuthorizationServer
@Configuration
public class OAuth2AuthorizationConfig extends AuthorizationServerConfigurerAdapter{
	
	@Autowired
	@Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired 
    private RedisConnectionFactory redisConnectionFactory;
    
    @Bean
    RedisTokenStore redisTokenStore(){
        return new RedisTokenStore(redisConnectionFactory);
    }
    
    /**
     * 用来配置客户端详情信息，一般使用数据库来存储或读取应用配置的详情信息（client_id ，client_secret，redirect_uri 等配置信息）。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	// 将客户端的信息存储在内存中
        clients.inMemory()
                // 配置一个客户端
                .withClient("userservice")
                .secret(new BCryptPasswordEncoder().encode("123456"))
                // 配置客户端的域
                .scopes("service")
                 // 配置验证类型为refresh_token和password
                .authorizedGrantTypes("refresh_token", "password","authorization_code","client_credentials")
                .redirectUris("http://www.baidu.com")
                // 配置token的过期时间为1h
                .accessTokenValiditySeconds(3600 * 1000);
    }
    
    /**
     * 用来配置授权以及令牌（Token）的访问端点和令牌服务（比如：配置令牌的签名与存储方式）
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	// 配置token的存储方式为JwtTokenStore
        endpoints.tokenStore(tokenStore())
                 // 配置用于JWT私钥加密的增强器
                 .tokenEnhancer(jwtTokenEnhancer())
                 // 配置安全认证管理
                 .authenticationManager(authenticationManager);
    }
    
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }
    
    public static void main(String[] args) {
		System.out.println(new BCryptPasswordEncoder().encode("123456"));
	}
    
    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        // 配置jks文件
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("fzp-jwt.jks"), "fzp123".toCharArray());
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("fzp-jwt"));
        return converter;
    }
    
}
