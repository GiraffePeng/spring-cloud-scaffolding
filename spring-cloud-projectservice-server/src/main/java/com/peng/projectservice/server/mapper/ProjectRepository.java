package com.peng.projectservice.server.mapper;

import org.springframework.data.repository.CrudRepository;

import com.peng.projectservice.entity.ProjectEntity;

public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {
}
