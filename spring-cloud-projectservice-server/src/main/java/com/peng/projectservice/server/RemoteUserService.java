package com.peng.projectservice.server;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

import com.peng.userservice.entity.User;
import com.peng.userservice.service.UserService;

import java.math.BigDecimal;

@FeignClient(value = "userservice",fallback = RemoteUserService.Fallback.class)
public interface RemoteUserService extends UserService {
    @Component
    class Fallback implements RemoteUserService {

        @Override
        public User getUser(long id) throws Exception {
            return null;
        }

        @Override
        public BigDecimal consumeMoney(long id, BigDecimal amount) throws Exception {
            return null;
        }

        @Override
        public BigDecimal lendpayMoney(long investorId, long borrowerId, BigDecimal amount) throws Exception {
            return null;
        }
    }
}

