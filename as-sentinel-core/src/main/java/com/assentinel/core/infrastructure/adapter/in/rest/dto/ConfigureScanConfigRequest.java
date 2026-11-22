package com.assentinel.core.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConfigureScanConfigRequest(
    @NotEmpty(message = "At least one file extension must be configured")
    List<String> extensions,

    List<String> exclusions,

    @Min(value = 1, message = "topKRules must be at least 1")
    @Max(value = 10, message = "topKRules must be at most 10")
    int topKRules,

    @DecimalMin(value = "0.0", message = "scoreThreshold must be at least 0.0")
    @DecimalMax(value = "1.0", message = "scoreThreshold must be at most 1.0")
    double scoreThreshold
) {
}
