package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.port.out.RepositorySourceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RepositorySourceRepositoryAdapter implements RepositorySourceRepositoryPort {

    private final JpaRepositorySourceRepository sourceRepository;
    private final JpaProjectRepository projectRepository;

    public RepositorySourceRepositoryAdapter(
        JpaRepositorySourceRepository sourceRepository,
        JpaProjectRepository projectRepository
    ) {
        this.sourceRepository = sourceRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public RepositorySource save(RepositorySource source) {
        ProjectEntity project = projectRepository.findById(source.projectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + source.projectId()));

        Optional<RepositorySourceEntity> existing =
                sourceRepository.findByProject_Id(source.projectId());
        RepositorySourceEntity entity = existing.orElseGet(() ->
                new RepositorySourceEntity(
                        source.id(),
                        project,
                        source.repositoryUrl(),
                        source.branch(),
                        source.localDirectory(),
                        source.status(),
                        source.updatedAt()));

        if (existing.isPresent()) {
            entity = new RepositorySourceEntity(
                    source.id(),
                    project,
                    source.repositoryUrl(),
                    source.branch(),
                    source.localDirectory(),
                    source.status(),
                    source.updatedAt());
        }

        return toDomain(sourceRepository.save(entity));
    }

    @Override
    public Optional<RepositorySource> findByProjectId(UUID projectId) {
        return sourceRepository.findByProject_Id(projectId).map(this::toDomain);
    }

    @Override
    public void deleteByProjectId(UUID projectId) {
        sourceRepository.findByProject_Id(projectId)
                .ifPresent(sourceRepository::delete);
    }

    private RepositorySource toDomain(RepositorySourceEntity entity) {
        return new RepositorySource(
                entity.getId(),
                entity.getProject().getId(),
                entity.getRepositoryUrl(),
                entity.getBranch(),
                entity.getLocalDirectory(),
                entity.getStatus(),
                entity.getUpdatedAt());
    }
}
