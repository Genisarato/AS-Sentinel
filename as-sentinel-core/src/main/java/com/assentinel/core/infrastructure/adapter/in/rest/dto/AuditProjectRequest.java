package com.assentinel.core.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AuditProjectRequest(
    @NotBlank(message = "Directory path must not be blank")
    String directoryPath,

    @Min(value = 1, message = "topKRules must be at least 1")
    @Max(value = 10, message = "topKRules must be at most 10")
    Integer topKRules,

    @DecimalMin(value = "0.0", message = "scoreThreshold must be at least 0.0")
    @DecimalMax(value = "1.0", message = "scoreThreshold must be at most 1.0")
    Double scoreThreshold
) {
    public AuditProjectRequest(String directoryPath) {
        this(directoryPath, null, null);
    }
}
