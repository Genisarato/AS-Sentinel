package com.assentinel.core.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Project(
    UUID id,
    String name,
    String repositoryUrl,
    Instant createdAt
) {
    public Project {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank");
        }
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new IllegalArgumentException("Repository URL must not be blank");
        }
    }
}
