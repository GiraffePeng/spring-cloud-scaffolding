package com.peng.projectservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

import com.peng.userservice.entity.User;
import com.peng.userservice.service.UserService;

import java.math.BigDecimal;

@FeignClient(value = "userservice", fallback = RemoteUserService.Fallback.class)
public interface RemoteUserService extends UserService {
    @Component
    @Slf4j
    class Fallback implements RemoteUserService {

        @Override
        public User getUser(long id) throws Exception {
            log.warn("getUser fallback");
            return null;
        }

        @Override
        public BigDecimal consumeMoney(long id, BigDecimal amount) throws Exception {
            log.warn("consumeMoney fallback");
            return null;
        }

        @Override
        public BigDecimal lendpayMoney(long investorId, long borrowerId, BigDecimal amount) throws Exception {
            log.warn("lendpayMoney fallback");
            return null;
        }
    }
}
