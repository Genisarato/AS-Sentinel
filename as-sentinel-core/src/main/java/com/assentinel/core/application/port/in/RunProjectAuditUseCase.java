package com.assentinel.core.application.port.in;

import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditRun;

import java.util.UUID;

public interface RunProjectAuditUseCase {
    AuditRun execute(UUID projectId, AuditRequest request);
}
