package com.assentinel.core.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuditRun(
    UUID id,
    UUID projectId,
    Instant createdAt,
    AuditReport report
) {
    public AuditRun {
        if (id == null) {
            throw new IllegalArgumentException("Audit run ID must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created-at timestamp must not be null");
        }
        if (report == null) {
            throw new IllegalArgumentException("Audit report must not be null");
        }
    }
}
