package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;

public interface JpaAuditRunRepository extends JpaRepository<AuditRunEntity, UUID> {
    @EntityGraph(attributePaths = {"project", "findings", "findings.violations"})
    List<AuditRunEntity> findByProject_IdOrderByCreatedAtDesc(UUID projectId);

    @Override
    @EntityGraph(attributePaths = {"project", "findings", "findings.violations"})
    java.util.Optional<AuditRunEntity> findById(UUID id);
}
