package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaProjectRepository extends JpaRepository<ProjectEntity, UUID> {
}
