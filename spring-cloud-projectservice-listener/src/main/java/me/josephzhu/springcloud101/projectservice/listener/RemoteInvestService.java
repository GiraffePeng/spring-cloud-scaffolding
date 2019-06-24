package me.josephzhu.springcloud101.projectservice.listener;

import org.springframework.cloud.openfeign.FeignClient;

import com.peng.investservice.service.InvestService;

@FeignClient(value = "investservice")
public interface RemoteInvestService extends InvestService {
}
