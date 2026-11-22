package com.assentinel.core.domain.model;

import java.time.Instant;
import java.util.UUID;

public record RepositorySource(
    UUID id,
    UUID projectId,
    String repositoryUrl,
    String branch,
    String localDirectory,
    RepositorySourceStatus status,
    Instant updatedAt
) {
    public RepositorySource {
        if (id == null) {
            throw new IllegalArgumentException("Repository source ID must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID must not be null");
        }
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new IllegalArgumentException("Repository URL must not be blank");
        }
        if (branch == null || branch.isBlank()) {
            throw new IllegalArgumentException("Branch must not be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("Repository source status must not be null");
        }
    }
}
