package com.assentinel.core.application.service;

import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.application.port.in.RepositorySourceUseCase;
import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.model.RepositorySourceStatus;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import com.assentinel.core.domain.port.out.RepositorySourceRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RepositorySourceService implements RepositorySourceUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final RepositorySourceRepositoryPort sourceRepositoryPort;

    public RepositorySourceService(
        ProjectRepositoryPort projectRepositoryPort,
        RepositorySourceRepositoryPort sourceRepositoryPort
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.sourceRepositoryPort = sourceRepositoryPort;
    }

    @Override
    public RepositorySource configure(
        UUID projectId,
        String repositoryUrl,
        String branch,
        String localDirectory
    ) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        RepositorySource source = new RepositorySource(
                UUID.randomUUID(),
                projectId,
                repositoryUrl,
                branch,
                localDirectory,
                RepositorySourceStatus.CONFIGURED,
                Instant.now());
        return sourceRepositoryPort.save(source);
    }

    @Override
    public Optional<RepositorySource> findByProjectId(UUID projectId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        return sourceRepositoryPort.findByProjectId(projectId);
    }

    @Override
    public RepositorySource updateStatus(UUID projectId, RepositorySourceStatus status) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        RepositorySource source = sourceRepositoryPort.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Repository source not configured for project " + projectId));

        RepositorySource updated = new RepositorySource(
                source.id(),
                source.projectId(),
                source.repositoryUrl(),
                source.branch(),
                source.localDirectory(),
                status,
                Instant.now());
        return sourceRepositoryPort.save(updated);
    }

    @Override
    public void delete(UUID projectId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        sourceRepositoryPort.deleteByProjectId(projectId);
    }
}
