package com.peng.userservice.server;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peng.userservice.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.math.BigDecimal;
import java.util.UUID;
import com.peng.userservice.entity.User;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@Api(tags = "用户平台接口")
public class UserServiceController implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserServiceDetail userServiceDetail;
    @Autowired
    RedissonClient redissonClient;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册接口", httpMethod = "POST")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", defaultValue = "请输入用户名"),
        @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", defaultValue = "请输入密码")
    })
    public com.peng.userservice.server.User postUser(@RequestParam("username") String username,
                         @RequestParam("password") String password){
       return userServiceDetail.insertUser(username, password);
    }
    
    @PostMapping("/login")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", defaultValue = "请输入用户名"),
        @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", defaultValue = "请输入密码")
    })
    @ApiOperation(value = "用户登陆接口", httpMethod = "POST")
    public UserLoginDTO login(@RequestParam("username") String username,
                              @RequestParam("password") String password,
                              HttpServletRequest request) {
        return userServiceDetail.login(username,password);
    }
    
    @RequestMapping(value = "/foo", method = RequestMethod.GET)
    public String getFoo() {
        return "i'm foo, " + UUID.randomUUID().toString();
    }

    @Override
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户主键", required = true, dataType = "long", defaultValue = "1")
    })
    @ApiOperation(value = "获取用户信息接口", httpMethod = "POST")
    public User getUser(long id) {
        return userRepository.findById(id).map(userEntity ->
                User.builder()
                        .id(userEntity.getId())
                        .availableBalance(userEntity.getAvailableBalance())
                        .frozenBalance(userEntity.getFrozenBalance())
                        .name(userEntity.getName())
                        .createdAt(userEntity.getCreatedAt())
                        .build())
                .orElse(null);
    }

    public BigDecimal consumeMoney(long investorId, BigDecimal amount) {
        RLock lock = redissonClient.getLock("User" + investorId);
        lock.lock();
        try {
            UserEntity user = userRepository.findById(investorId).orElse(null);
            if (user != null && user.getAvailableBalance().compareTo(amount)>=0) {
                user.setAvailableBalance(user.getAvailableBalance().subtract(amount));
                user.setFrozenBalance(user.getFrozenBalance().add(amount));
                userRepository.save(user);
                return amount;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal lendpayMoney(long investorId, long borrowerId, BigDecimal amount) throws Exception {
        RLock lock = redissonClient.getLock("User" + investorId);
        lock.lock();
        try {
            UserEntity investor = userRepository.findById(investorId).orElse(null);
            UserEntity borrower = userRepository.findById(borrowerId).orElse(null);

            if (investor != null && borrower != null && investor.getFrozenBalance().compareTo(amount) >= 0) {
                investor.setFrozenBalance(investor.getFrozenBalance().subtract(amount));
                userRepository.save(investor);
                borrower.setAvailableBalance(borrower.getAvailableBalance().add(amount));
                userRepository.save(borrower);
                return amount;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

}
