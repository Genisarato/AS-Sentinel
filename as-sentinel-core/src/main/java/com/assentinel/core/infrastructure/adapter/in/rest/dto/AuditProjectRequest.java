package com.assentinel.core.infrastructure.adapter.in.rest.dto;

public record AuditProjectRequest(
    String directoryPath,
    Integer topKRules,
    Double scoreThreshold
) {
    public AuditProjectRequest(String directoryPath) {
        this(directoryPath, null, null);
    }
}
