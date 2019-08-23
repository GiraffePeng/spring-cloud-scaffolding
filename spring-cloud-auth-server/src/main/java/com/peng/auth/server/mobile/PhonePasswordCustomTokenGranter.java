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
