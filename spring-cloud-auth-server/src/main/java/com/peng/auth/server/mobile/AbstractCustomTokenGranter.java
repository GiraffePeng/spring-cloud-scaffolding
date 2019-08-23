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
