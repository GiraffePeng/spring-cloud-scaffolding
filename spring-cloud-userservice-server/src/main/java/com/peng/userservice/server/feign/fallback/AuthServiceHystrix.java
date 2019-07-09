package com.peng.userservice.server.feign.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.peng.userservice.dto.JWT;
import com.peng.userservice.server.feign.AuthServiceClient;

@Component
public class AuthServiceHystrix implements AuthServiceClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceHystrix.class);

    @Override
    public JWT getToken(String authorization, String type, String username, String password) {
        LOGGER.warn("Fallback of getToken is executed");
        return null;
    }
}
