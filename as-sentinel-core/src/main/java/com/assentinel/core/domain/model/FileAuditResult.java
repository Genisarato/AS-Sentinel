package com.assentinel.core.domain.model;

import java.util.List;

public record FileAuditResult(
    String filePath,
    boolean compliant,
    List<RuleViolation> violations
) {}
