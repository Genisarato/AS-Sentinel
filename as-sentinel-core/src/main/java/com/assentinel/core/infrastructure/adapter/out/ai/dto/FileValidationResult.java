package com.assentinel.core.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FileValidationResult(
    @JsonProperty("file_path") String filePath,
    @JsonProperty("is_compliant") boolean compliant,
    List<RuleViolation> violations
) {}
