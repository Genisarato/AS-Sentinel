package com.assentinel.core.domain.model;

public record RuleViolation(
    String ruleId,
    String severity,
    String reason,
    String suggestion
) {}
