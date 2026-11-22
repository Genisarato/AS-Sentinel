package com.assentinel.core.application.port.in;

import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.model.RepositorySourceStatus;

import java.util.Optional;
import java.util.UUID;

public interface RepositorySourceUseCase {
    RepositorySource configure(
        UUID projectId,
        String repositoryUrl,
        String branch,
        String localDirectory
    );
    Optional<RepositorySource> findByProjectId(UUID projectId);
    RepositorySource updateStatus(UUID projectId, RepositorySourceStatus status);
    void delete(UUID projectId);
}
