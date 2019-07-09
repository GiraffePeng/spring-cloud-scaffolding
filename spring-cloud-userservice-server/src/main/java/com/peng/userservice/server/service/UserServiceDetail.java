package com.peng.userservice.server.service;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.peng.userservice.dto.JWT;
import com.peng.userservice.dto.User;
import com.peng.userservice.dto.UserLoginDTO;
import com.peng.userservice.server.exception.UserLoginException;
import com.peng.userservice.server.feign.AuthServiceClient;
import com.peng.userservice.server.mapper.UserAuthRepository;
import com.peng.userservice.server.util.BPwdEncoderUtil;

@Service
public class UserServiceDetail {
	 @Autowired
	 private UserAuthRepository userAuthRepository;
	 
	 @Autowired
	 private AuthServiceClient client;

	 public User insertUser(String username,String  password){
	        User user=new User();
	        user.setUsername(username);
	        user.setPassword(BPwdEncoderUtil.BCryptPassword(password));
	        return userAuthRepository.save(user);
	 }
	 
	 public UserLoginDTO login(String username, String password) {
	        // 查询数据库
	        User user = null;
	        Iterator<User> iterator = userAuthRepository.findAll().iterator();
	        while(iterator.hasNext()) {
	        	User next = iterator.next();
	        	if(next.getUsername().equals(username)) {
	        		user = next;
	        	}
	        }
	        if (user == null) {
	            throw new UserLoginException("error username");
	        }

	        if(!BPwdEncoderUtil.matches(password,user.getPassword())){
	            throw new UserLoginException("error password");
	        }

	        // 从auth-service获取JWT
	        JWT jwt = client.getToken("Basic dXNlcnNlcnZpY2U6MTIzNDU2", "password", username, password);
	        if(jwt == null){
	            throw new UserLoginException("error internal");
	        }

	        UserLoginDTO userLoginDTO=new UserLoginDTO();
	        userLoginDTO.setJwt(jwt);
	        userLoginDTO.setUser(user);
	        return userLoginDTO;
	 }
	 
}
