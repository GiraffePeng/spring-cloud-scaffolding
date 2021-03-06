package com.peng.userservice.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.peng.userservice.entity.User;

import java.math.BigDecimal;

public interface UserService {
    @GetMapping("/user/getUser")
    User getUser(@RequestParam("id") long id) throws Exception;
    @PostMapping("/user/consumeMoney")
    BigDecimal consumeMoney(@RequestParam("investorId") long investorId,
                            @RequestParam("amount") BigDecimal amount) throws Exception;
    @PostMapping("/user/lendpayMoney")
    BigDecimal lendpayMoney(@RequestParam("investorId") long investorId,
                            @RequestParam("borrowerId") long borrowerId,
                            @RequestParam("amount") BigDecimal amount) throws Exception;
}
