package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaScanConfigRepository extends JpaRepository<ScanConfigEntity, UUID> {
    @EntityGraph(attributePaths = {"project", "extensions", "exclusions"})
    Optional<ScanConfigEntity> findByProject_Id(UUID projectId);
}
