package com.peng.investservice.server.mapper;

import org.springframework.data.repository.CrudRepository;

import com.peng.investservice.entity.InvestEntity;

import java.util.List;

public interface InvestRepository extends CrudRepository<InvestEntity, Long> {
    List<InvestEntity> findByProjectIdAndStatus(long projectId, int status);
}
