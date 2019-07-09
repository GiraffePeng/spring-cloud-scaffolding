package com.peng.userservice.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class JWT implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String scope;
    private String jti;
}
