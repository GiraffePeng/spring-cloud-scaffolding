package com.peng.investservice.server;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InvestRepository extends CrudRepository<InvestEntity, Long> {
    List<InvestEntity> findByProjectIdAndStatus(long projectId, int status);
}
