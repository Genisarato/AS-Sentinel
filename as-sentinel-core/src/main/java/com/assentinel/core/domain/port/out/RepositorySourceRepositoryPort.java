package com.assentinel.core.domain.port.out;

import com.assentinel.core.domain.model.RepositorySource;

import java.util.Optional;
import java.util.UUID;

public interface RepositorySourceRepositoryPort {
    RepositorySource save(RepositorySource source);
    Optional<RepositorySource> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
