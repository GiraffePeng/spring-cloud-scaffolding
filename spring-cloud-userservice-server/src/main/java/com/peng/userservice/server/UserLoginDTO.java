package com.peng.userservice.server;

import lombok.Data;

@Data
public class UserLoginDTO {
	private JWT jwt;
    private User user;
}
