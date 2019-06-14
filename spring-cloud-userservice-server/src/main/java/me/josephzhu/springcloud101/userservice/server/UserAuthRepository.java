package me.josephzhu.springcloud101.userservice.server;

import org.springframework.data.repository.CrudRepository;

public interface UserAuthRepository extends CrudRepository<User, Long> {
}
