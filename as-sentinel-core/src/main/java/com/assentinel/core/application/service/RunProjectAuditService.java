package com.assentinel.core.application.service;

import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.application.port.in.RunProjectAuditUseCase;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditRun;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import com.assentinel.core.domain.port.out.AuditRunRepositoryPort;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RunProjectAuditService implements RunProjectAuditUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ArchitectureRulesPort architectureRulesPort;
    private final AuditRunRepositoryPort auditRunRepositoryPort;

    public RunProjectAuditService(
        ProjectRepositoryPort projectRepositoryPort,
        ArchitectureRulesPort architectureRulesPort,
        AuditRunRepositoryPort auditRunRepositoryPort
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.architectureRulesPort = architectureRulesPort;
        this.auditRunRepositoryPort = auditRunRepositoryPort;
    }

    @Override
    public AuditRun execute(UUID projectId, AuditRequest request) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        AuditReport report = architectureRulesPort.auditProject(request);
        AuditRun auditRun = new AuditRun(
                UUID.randomUUID(),
                projectId,
                Instant.now(),
                report);

        return auditRunRepositoryPort.save(auditRun);
    }
}
