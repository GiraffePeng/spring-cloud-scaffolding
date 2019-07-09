package com.peng.projectservice.listener.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.peng.investservice.service.InvestService;

@FeignClient(value = "investservice")
public interface RemoteInvestService extends InvestService {
}
