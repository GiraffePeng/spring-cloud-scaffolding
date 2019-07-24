package com.peng.auth.server.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * 这段代码非常简单，就是把用户信息以userDetails这个Key存放到Token中去（如果授权模式是客户端模式这段代码无效，因为和用户没关系）。
 * 这是一个常见需求，默认情况下Token中只会有用户名这样的基本信息，
 * 我们往往需要把有关用户的更多信息返回给客户端（在实际应用中你可能会从数据库或外部服务查询更多的用户信息加入到JWT Token中去），
 * 这个时候就可以自定义增强器来丰富Token的内容。
 *
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
