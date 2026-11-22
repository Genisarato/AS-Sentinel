package com.assentinel.core.application.exception;

import java.util.UUID;

public class AuditRunNotFoundException extends RuntimeException {
    public AuditRunNotFoundException(UUID projectId, UUID auditId) {
        super("Audit run not found: " + auditId + " for project " + projectId);
    }
}
