package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaRepositorySourceRepository
        extends JpaRepository<RepositorySourceEntity, UUID> {
    @EntityGraph(attributePaths = "project")
    Optional<RepositorySourceEntity> findByProject_Id(UUID projectId);
}
