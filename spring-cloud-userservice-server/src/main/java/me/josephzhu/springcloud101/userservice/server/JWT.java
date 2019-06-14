package me.josephzhu.springcloud101.userservice.server;

import lombok.Data;

@Data
public class JWT {
	private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String scope;
    private String jti;
}
