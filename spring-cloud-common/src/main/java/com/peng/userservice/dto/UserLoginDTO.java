package com.peng.userservice.dto;

import java.io.Serializable;
import com.peng.userservice.dto.User;

import lombok.Data;

@Data
public class UserLoginDTO implements Serializable{
	private JWT jwt;
    private User user;
}
