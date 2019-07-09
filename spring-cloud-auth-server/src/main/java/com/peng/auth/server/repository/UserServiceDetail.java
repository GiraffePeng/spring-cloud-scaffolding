package com.peng.auth.server.repository;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceDetail implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Iterator<User> iterator = userRepository.findAll().iterator();
		User user = null;
		while(iterator.hasNext()) {
			User next = iterator.next();
			if(username.equals(next.getUsername())) {
				user = next;
				break;
			}
		}
		return user;
	}

}
