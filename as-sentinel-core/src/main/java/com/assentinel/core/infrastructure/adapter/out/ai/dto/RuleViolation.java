package com.assentinel.core.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RuleViolation(
    @JsonProperty("rule_id") String ruleId,
    String severity,
    String reason,
    String suggestion
) {}
