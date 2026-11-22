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
import java.util.List;
import java.util.UUID;
import com.assentinel.core.application.exception.AuditRunNotFoundException;
import com.assentinel.core.application.exception.RepositoryScanConfigurationException;
import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.model.ScanConfig;
import com.assentinel.core.domain.model.RepositorySourceStatus;
import com.assentinel.core.domain.port.out.RepositorySourceRepositoryPort;
import com.assentinel.core.domain.port.out.ScanConfigRepositoryPort;

@Service
public class RunProjectAuditService implements RunProjectAuditUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ArchitectureRulesPort architectureRulesPort;
    private final AuditRunRepositoryPort auditRunRepositoryPort;
    private final RepositorySourceRepositoryPort repositorySourceRepositoryPort;
    private final ScanConfigRepositoryPort scanConfigRepositoryPort;

    public RunProjectAuditService(
        ProjectRepositoryPort projectRepositoryPort,
        ArchitectureRulesPort architectureRulesPort,
        AuditRunRepositoryPort auditRunRepositoryPort,
        RepositorySourceRepositoryPort repositorySourceRepositoryPort,
        ScanConfigRepositoryPort scanConfigRepositoryPort
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.architectureRulesPort = architectureRulesPort;
        this.auditRunRepositoryPort = auditRunRepositoryPort;
        this.repositorySourceRepositoryPort = repositorySourceRepositoryPort;
        this.scanConfigRepositoryPort = scanConfigRepositoryPort;
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

    @Override
    public AuditRun executeWithConfiguration(UUID projectId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        RepositorySource source = repositorySourceRepositoryPort.findByProjectId(projectId)
                .orElseThrow(() -> new RepositoryScanConfigurationException(
                        projectId, "repository source is missing"));
        ScanConfig config = scanConfigRepositoryPort.findByProjectId(projectId)
                .orElseThrow(() -> new RepositoryScanConfigurationException(
                        projectId, "scan config is missing"));

        if (source.status() != RepositorySourceStatus.CLONED) {
            throw new RepositoryScanConfigurationException(
                    projectId, "repository source must be CLONED");
        }
        if (source.localDirectory() == null || source.localDirectory().isBlank()) {
            throw new RepositoryScanConfigurationException(
                    projectId, "local directory is missing");
        }

        AuditRequest request = new AuditRequest(
                source.localDirectory(),
                config.topKRules(),
                config.scoreThreshold());
        AuditReport report = architectureRulesPort.auditProject(request);
        AuditRun auditRun = new AuditRun(
                UUID.randomUUID(),
                projectId,
                Instant.now(),
                report);
        return auditRunRepositoryPort.save(auditRun);
    }

    @Override
    public List<AuditRun> findByProjectId(UUID projectId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        return auditRunRepositoryPort.findByProjectId(projectId);
    }

    @Override
    public AuditRun findByProjectAndId(UUID projectId, UUID auditId) {
        projectRepositoryPort.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        return auditRunRepositoryPort.findById(auditId)
                .filter(auditRun -> auditRun.projectId().equals(projectId))
                .orElseThrow(() -> new AuditRunNotFoundException(projectId, auditId));
    }
}
