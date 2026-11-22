package com.assentinel.core.domain.port.out;

import com.assentinel.core.domain.model.AuditRun;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditRunRepositoryPort {
    AuditRun save(AuditRun auditRun);
    Optional<AuditRun> findById(UUID id);
    List<AuditRun> findByProjectId(UUID projectId);
}
