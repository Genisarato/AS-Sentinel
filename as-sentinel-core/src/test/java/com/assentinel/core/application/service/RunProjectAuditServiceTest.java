package com.assentinel.core.application.service;

import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditRun;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import com.assentinel.core.domain.port.out.AuditRunRepositoryPort;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.assentinel.core.application.exception.AuditRunNotFoundException;

class RunProjectAuditServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final ArchitectureRulesPort architectureRules = mock(ArchitectureRulesPort.class);
    private final AuditRunRepositoryPort auditRunRepository = mock(AuditRunRepositoryPort.class);
    private final RunProjectAuditService service = new RunProjectAuditService(
            projectRepository,
            architectureRules,
            auditRunRepository);

    @Test
    void runsAndPersistsAuditForExistingProject() {
        UUID projectId = UUID.randomUUID();
        AuditRequest request = new AuditRequest("/workspace/project");
        Project project = new Project(
                projectId,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now());
        AuditReport report = new AuditReport(
                "/workspace/project",
                1,
                0,
                true,
                List.of());

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(architectureRules.auditProject(request)).willReturn(report);
        given(auditRunRepository.save(any(AuditRun.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AuditRun result = service.execute(projectId, request);

        assertThat(result.projectId()).isEqualTo(projectId);
        assertThat(result.report()).isSameAs(report);
        verify(auditRunRepository).save(any(AuditRun.class));
    }

    @Test
    void rejectsAuditWhenProjectDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        AuditRequest request = new AuditRequest("/workspace/project");
        given(projectRepository.findById(projectId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(projectId, request))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(architectureRules, never()).auditProject(any(AuditRequest.class));
        verify(auditRunRepository, never()).save(any(AuditRun.class));
    }

    @Test
    void listsAuditsOnlyForExistingProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project(
                projectId,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now());
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(auditRunRepository.findByProjectId(projectId)).willReturn(List.of());

        assertThat(service.findByProjectId(projectId)).isEmpty();
        verify(auditRunRepository).findByProjectId(projectId);
    }

    @Test
    void rejectsAuditRunThatBelongsToAnotherProject() {
        UUID projectId = UUID.randomUUID();
        UUID auditId = UUID.randomUUID();
        UUID otherProjectId = UUID.randomUUID();
        Project project = new Project(
                projectId,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now());
        AuditRun auditRun = new AuditRun(
                auditId,
                otherProjectId,
                Instant.now(),
                new AuditReport("/workspace/project", 0, 0, true, List.of()));

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(auditRunRepository.findById(auditId)).willReturn(Optional.of(auditRun));

        assertThatThrownBy(() -> service.findByProjectAndId(projectId, auditId))
                .isInstanceOf(AuditRunNotFoundException.class);
    }
}
