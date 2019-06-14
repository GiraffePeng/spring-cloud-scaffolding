package me.josephzhu.springcloud101.userservice.server;

import lombok.Data;

@Data
public class UserLoginDTO {
	private JWT jwt;
    private User user;
}
