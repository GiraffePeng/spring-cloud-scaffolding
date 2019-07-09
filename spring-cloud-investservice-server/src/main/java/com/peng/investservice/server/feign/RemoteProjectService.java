package com.peng.investservice.server.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

import com.peng.projectservice.entity.Project;
import com.peng.projectservice.service.ProjectService;

import java.math.BigDecimal;

@Component
@FeignClient(value = "projectservice", fallback = RemoteProjectService.Fallback.class)
public interface RemoteProjectService extends ProjectService {
    @Component
    class Fallback implements RemoteProjectService {

        @Override
        public Project getProject(long id) throws Exception {
            return null;
        }

        @Override
        public BigDecimal gotInvested(long id, BigDecimal amount) throws Exception {
            return null;
        }

        @Override
        public BigDecimal lendpay(long id) throws Exception {
            return null;
        }
    }
}
