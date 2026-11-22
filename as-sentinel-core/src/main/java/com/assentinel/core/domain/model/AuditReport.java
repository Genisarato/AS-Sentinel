package com.assentinel.core.domain.model;

import java.util.List;

public record AuditReport(
    String directoryPath,
    int totalFilesScanned,
    int totalViolations,
    boolean projectCompliant,
    List<Finding> findings
) {}
