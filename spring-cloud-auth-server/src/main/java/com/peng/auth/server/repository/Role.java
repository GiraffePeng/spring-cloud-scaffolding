package com.peng.auth.server.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import lombok.Data;

@Entity
@Table(name = "role_auth")
public class Role implements GrantedAuthority{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String authority;
    
    @Column(nullable = false)
    private Long userId;
    

	public void setId(Long id) {
		this.id = id;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String getAuthority() {
		return authority;
	}
	
	@Override
	public String toString() {
		return authority;
	}

}
