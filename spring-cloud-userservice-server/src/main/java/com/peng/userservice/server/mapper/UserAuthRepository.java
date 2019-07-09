package com.peng.userservice.server.mapper;

import org.springframework.data.repository.CrudRepository;

import com.peng.userservice.dto.User;

public interface UserAuthRepository extends CrudRepository<User, Long> {
}
