package com.peng.userservice.server.mapper;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.repository.CrudRepository;

import com.peng.userservice.entity.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
}
