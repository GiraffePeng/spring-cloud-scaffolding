package com.peng.userservice.server;

import org.springframework.data.repository.CrudRepository;

public interface UserAuthRepository extends CrudRepository<User, Long> {
}
