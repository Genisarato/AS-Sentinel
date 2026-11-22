package com.assentinel.core.domain.model;

import java.util.List;

public record Finding(
    String filePath,
    boolean compliant,
    List<RuleViolation> violations
) {
}
