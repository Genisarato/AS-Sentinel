package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.ScanConfig;
import com.assentinel.core.domain.port.out.ScanConfigRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Component
public class ScanConfigRepositoryAdapter implements ScanConfigRepositoryPort {

    private final JpaScanConfigRepository scanConfigRepository;
    private final JpaProjectRepository projectRepository;

    public ScanConfigRepositoryAdapter(
        JpaScanConfigRepository scanConfigRepository,
        JpaProjectRepository projectRepository
    ) {
        this.scanConfigRepository = scanConfigRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public ScanConfig save(ScanConfig config) {
        ProjectEntity project = projectRepository.findById(config.projectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + config.projectId()));

        Optional<ScanConfigEntity> existing =
                scanConfigRepository.findByProject_Id(config.projectId());
        ScanConfigEntity entity = existing.orElseGet(() ->
                new ScanConfigEntity(
                        config.id(),
                        project,
                        config.extensions(),
                        config.exclusions(),
                        config.topKRules(),
                        config.scoreThreshold(),
                        config.updatedAt()));

        if (existing.isPresent()) {
            entity = new ScanConfigEntity(
                    config.id(),
                    project,
                    config.extensions(),
                    config.exclusions(),
                    config.topKRules(),
                    config.scoreThreshold(),
                    config.updatedAt());
        }

        return toDomain(scanConfigRepository.save(entity));
    }

    @Override
    public Optional<ScanConfig> findByProjectId(UUID projectId) {
        return scanConfigRepository.findByProject_Id(projectId).map(this::toDomain);
    }

    @Override
    public void deleteByProjectId(UUID projectId) {
        scanConfigRepository.findByProject_Id(projectId)
                .ifPresent(scanConfigRepository::delete);
    }

    private ScanConfig toDomain(ScanConfigEntity entity) {
        return new ScanConfig(
                entity.getId(),
                entity.getProject().getId(),
                new ArrayList<>(entity.getExtensions()),
                new ArrayList<>(entity.getExclusions()),
                entity.getTopKRules(),
                entity.getScoreThreshold(),
                entity.getUpdatedAt());
    }
}
