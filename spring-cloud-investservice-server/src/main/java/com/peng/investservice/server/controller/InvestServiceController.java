package com.peng.investservice.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.peng.investservice.entity.Invest;
import com.peng.investservice.entity.InvestEntity;
import com.peng.investservice.server.feign.RemoteProjectService;
import com.peng.investservice.server.feign.RemoteUserService;
import com.peng.investservice.server.mapper.InvestRepository;
import com.peng.investservice.service.InvestService;
import com.peng.projectservice.entity.Project;
import com.peng.userservice.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class InvestServiceController implements InvestService {
    @Autowired
    InvestRepository investRepository;
    @Autowired
    RemoteUserService remoteUserService;
    @Autowired
    RemoteProjectService remoteProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invest createOrder(long userId, long projectId, BigDecimal amount) throws Exception {
        User investor = remoteUserService.getUser(userId);
        if (investor == null) throw new Exception("无效用户ID");
        if (amount.compareTo(investor.getAvailableBalance()) > 0) throw new Exception("用户余额不足");

        Project project = remoteProjectService.getProject(projectId);
        if (project == null) throw new Exception("无效项目ID");
        if (amount.compareTo(project.getRemainAmount()) > 0) throw new Exception("项目余额不足");
        if (project.getStatus() !=1) throw new Exception("项目不是募集中状不能投资");

        InvestEntity investEntity = new InvestEntity();
        investEntity.setInvestorId(investor.getId());
        investEntity.setInvestorName(investor.getName());
        investEntity.setAmount(amount);
        investEntity.setBorrowerId(project.getBorrowerId());
        investEntity.setBorrowerName(project.getBorrowerName());
        investEntity.setProjectId(project.getId());
        investEntity.setProjectName(project.getName());
        investEntity.setStatus(1);
        investRepository.save(investEntity);

        if (remoteUserService.consumeMoney(userId, amount) == null) throw new Exception("用户消费失败");
        if (remoteProjectService.gotInvested(projectId, amount) == null) throw new Exception("项目投资失败");

        return Invest.builder()
                .id(investEntity.getId())
                .amount(investEntity.getAmount())
                .borrowerId(investEntity.getBorrowerId())
                .investorId(investEntity.getInvestorId())
                .projectId(investEntity.getProjectId())
                .status(investEntity.getStatus())
                .createdAt(investEntity.getCreatedAt())
                .updatedAt(investEntity.getUpdatedAt())
                .build();
    }

    @Override
    public List<Invest> getOrders(long projectId) throws Exception {
        return investRepository.findByProjectIdAndStatus(projectId,1).stream()
                .map(investEntity -> Invest.builder()
                        .id(investEntity.getId())
                        .amount(investEntity.getAmount())
                        .borrowerId(investEntity.getBorrowerId())
                        .investorId(investEntity.getInvestorId())
                        .projectId(investEntity.getProjectId())
                        .status(investEntity.getStatus())
                        .createdAt(investEntity.getCreatedAt())
                        .updatedAt(investEntity.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
