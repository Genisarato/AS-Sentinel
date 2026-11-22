package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRun;
import com.assentinel.core.domain.model.Finding;
import com.assentinel.core.domain.model.RuleViolation;
import com.assentinel.core.domain.port.out.AuditRunRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuditRunRepositoryAdapter implements AuditRunRepositoryPort {

    private final JpaAuditRunRepository auditRunRepository;
    private final JpaProjectRepository projectRepository;

    public AuditRunRepositoryAdapter(
        JpaAuditRunRepository auditRunRepository,
        JpaProjectRepository projectRepository
    ) {
        this.auditRunRepository = auditRunRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public AuditRun save(AuditRun auditRun) {
        ProjectEntity project = projectRepository.findById(auditRun.projectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + auditRun.projectId()));

        AuditReport report = auditRun.report();
        AuditRunEntity entity = new AuditRunEntity(
                auditRun.id(),
                project,
                auditRun.createdAt(),
                report.directoryPath(),
                report.totalFilesScanned(),
                report.totalViolations(),
                report.projectCompliant());

        for (Finding finding : report.findings()) {
            FindingEntity findingEntity = new FindingEntity(
                    UUID.randomUUID(),
                    finding.filePath(),
                    finding.compliant());

            for (RuleViolation violation : finding.violations()) {
                findingEntity.addViolation(new RuleViolationEntity(
                        UUID.randomUUID(),
                        violation.ruleId(),
                        violation.severity(),
                        violation.reason(),
                        violation.suggestion()));
            }
            entity.addFinding(findingEntity);
        }

        return toDomain(auditRunRepository.save(entity));
    }

    @Override
    public Optional<AuditRun> findById(UUID id) {
        return auditRunRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AuditRun> findByProjectId(UUID projectId) {
        return auditRunRepository.findByProject_IdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDomain)
                .toList();
    }

    private AuditRun toDomain(AuditRunEntity entity) {
        List<Finding> findings = entity.getFindings().stream()
                .map(finding -> new Finding(
                        finding.getFilePath(),
                        finding.isCompliant(),
                        finding.getViolations().stream()
                                .map(violation -> new RuleViolation(
                                        violation.getRuleId(),
                                        violation.getSeverity(),
                                        violation.getReason(),
                                        violation.getSuggestion()))
                                .toList()))
                .toList();

        AuditReport report = new AuditReport(
                entity.getDirectoryPath(),
                entity.getTotalFilesScanned(),
                entity.getTotalViolations(),
                entity.isProjectCompliant(),
                findings);

        return new AuditRun(
                entity.getId(),
                entity.getProject().getId(),
                entity.getCreatedAt(),
                report);
    }
}
