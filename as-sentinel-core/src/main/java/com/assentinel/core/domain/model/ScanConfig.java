package com.assentinel.core.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScanConfig(
    UUID id,
    UUID projectId,
    List<String> extensions,
    List<String> exclusions,
    int topKRules,
    double scoreThreshold,
    Instant updatedAt
) {
    public ScanConfig {
        if (id == null) {
            throw new IllegalArgumentException("Scan config ID must not be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID must not be null");
        }
        if (extensions == null || extensions.isEmpty()) {
            throw new IllegalArgumentException("At least one file extension must be configured");
        }
        if (exclusions == null) {
            exclusions = List.of();
        } else {
            exclusions = List.copyOf(exclusions);
        }
        extensions = List.copyOf(extensions);
        if (topKRules < 1 || topKRules > 10) {
            throw new IllegalArgumentException("topKRules must be between 1 and 10");
        }
        if (scoreThreshold < 0.0 || scoreThreshold > 1.0) {
            throw new IllegalArgumentException("scoreThreshold must be between 0.0 and 1.0");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("Updated-at timestamp must not be null");
        }
    }
}
