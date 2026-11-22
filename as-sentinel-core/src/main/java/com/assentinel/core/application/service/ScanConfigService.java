package com.assentinel.core.application.service;

import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.application.port.in.ScanConfigUseCase;
import com.assentinel.core.domain.model.ScanConfig;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import com.assentinel.core.domain.port.out.ScanConfigRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScanConfigService implements ScanConfigUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ScanConfigRepositoryPort scanConfigRepositoryPort;

    public ScanConfigService(
        ProjectRepositoryPort projectRepositoryPort,
        ScanConfigRepositoryPort scanConfigRepositoryPort
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.scanConfigRepositoryPort = scanConfigRepositoryPort;
    }

    @Override
    public ScanConfig configure(
        UUID projectId,
        List<String> extensions,
        List<String> exclusions,
        int topKRules,
        double scoreThreshold
    ) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        UUID configId = scanConfigRepositoryPort.findByProjectId(projectId)
                .map(ScanConfig::id)
                .orElseGet(UUID::randomUUID);

        ScanConfig config = new ScanConfig(
                configId,
                projectId,
                extensions,
                exclusions,
                topKRules,
                scoreThreshold,
                Instant.now());
        return scanConfigRepositoryPort.save(config);
    }

    @Override
    public Optional<ScanConfig> findByProjectId(UUID projectId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        return scanConfigRepositoryPort.findByProjectId(projectId);
    }

    @Override
    public void delete(UUID projectId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        scanConfigRepositoryPort.deleteByProjectId(projectId);
    }
}
