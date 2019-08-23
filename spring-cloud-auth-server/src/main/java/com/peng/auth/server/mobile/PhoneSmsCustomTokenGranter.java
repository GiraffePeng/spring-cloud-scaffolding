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
