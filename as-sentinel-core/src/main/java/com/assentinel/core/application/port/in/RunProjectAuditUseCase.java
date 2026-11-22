package com.assentinel.core.application.port.in;

import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditRun;

import java.util.UUID;
import java.util.List;

public interface RunProjectAuditUseCase {
    AuditRun execute(UUID projectId, AuditRequest request);
    AuditRun executeWithConfiguration(UUID projectId);
    List<AuditRun> findByProjectId(UUID projectId);
    AuditRun findByProjectAndId(UUID projectId, UUID auditId);
}
