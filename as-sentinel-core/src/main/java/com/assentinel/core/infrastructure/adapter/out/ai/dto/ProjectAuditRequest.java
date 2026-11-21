package com.assentinel.core.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectAuditRequest(
    @JsonProperty("directory_path") String directoryPath,
    @JsonProperty("top_k_rules") int topKRules,
    @JsonProperty("score_threshold") double scoreThreshold
) {
    public ProjectAuditRequest(String directoryPath) {
        this(directoryPath, 3, 0.35);
    }
}
