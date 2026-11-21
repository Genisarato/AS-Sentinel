package com.assentinel.core.domain.model;

public record AuditRequest(
    String directoryPath,
    int topKRules,
    double scoreThreshold
) {
    public AuditRequest {
        if (directoryPath == null || directoryPath.isBlank()) {
            throw new IllegalArgumentException("Directory path must not be blank");
        }
        if (topKRules < 1 || topKRules > 10) {
            throw new IllegalArgumentException("topKRules must be between 1 and 10");
        }
        if (scoreThreshold < 0.0 || scoreThreshold > 1.0) {
            throw new IllegalArgumentException("scoreThreshold must be between 0.0 and 1.0");
        }
    }

    public AuditRequest(String directoryPath) {
        this(directoryPath, 3, 0.35);
    }
}
