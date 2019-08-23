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
