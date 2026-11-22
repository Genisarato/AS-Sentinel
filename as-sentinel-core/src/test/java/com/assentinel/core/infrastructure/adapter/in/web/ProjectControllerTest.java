package com.assentinel.core.infrastructure.adapter.in.web;

import com.assentinel.core.application.port.in.ProjectUseCase;
import com.assentinel.core.application.port.in.RunProjectAuditUseCase;
import com.assentinel.core.application.port.in.RepositorySourceUseCase;
import com.assentinel.core.application.port.in.ScanConfigUseCase;
import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.model.RepositorySourceStatus;
import com.assentinel.core.domain.model.ScanConfig;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRun;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.infrastructure.adapter.in.rest.ProjectController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectUseCase projectUseCase;

    @MockitoBean
    private RunProjectAuditUseCase runProjectAuditUseCase;

    @MockitoBean
    private RepositorySourceUseCase repositorySourceUseCase;

    @MockitoBean
    private ScanConfigUseCase scanConfigUseCase;

    @Test
    void createsProject() throws Exception {
        UUID id = UUID.randomUUID();
        given(projectUseCase.create("AS-Sentinel", "https://github.com/example/as-sentinel"))
                .willReturn(new Project(
                        id,
                        "AS-Sentinel",
                        "https://github.com/example/as-sentinel",
                        Instant.parse("2026-11-22T09:41:00Z")));

        mockMvc.perform(post("/api/v1/projects")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "AS-Sentinel",
                                  "repositoryUrl": "https://github.com/example/as-sentinel"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("AS-Sentinel"));
    }

    @Test
    void runsAuditForProject() throws Exception {
        UUID id = UUID.randomUUID();
        AuditRun auditRun = new AuditRun(
                UUID.randomUUID(),
                id,
                Instant.parse("2026-11-22T09:41:00Z"),
                new AuditReport(
                        "/workspace/project",
                        1,
                        0,
                        true,
                        java.util.List.of()));

        given(runProjectAuditUseCase.execute(
                org.mockito.ArgumentMatchers.eq(id),
                org.mockito.ArgumentMatchers.any(AuditRequest.class)))
                .willReturn(auditRun);

        mockMvc.perform(post("/api/v1/projects/{id}/audits", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "directoryPath": "/workspace/project"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(id.toString()))
                .andExpect(jsonPath("$.report.projectCompliant").value(true));
    }

    @Test
    void rejectsBlankProjectName() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": " ",
                                  "repositoryUrl": "https://github.com/example/as-sentinel"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returnsNotFoundWhenProjectDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        given(runProjectAuditUseCase.execute(
                org.mockito.ArgumentMatchers.eq(id),
                org.mockito.ArgumentMatchers.any(AuditRequest.class)))
                .willThrow(new ProjectNotFoundException(id));

        mockMvc.perform(post("/api/v1/projects/{id}/audits", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "directoryPath": "/workspace/project"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listsAuditsForProject() throws Exception {
        UUID id = UUID.randomUUID();
        given(runProjectAuditUseCase.findByProjectId(id)).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/projects/{id}/audits", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getsOneAuditForProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID auditId = UUID.randomUUID();
        AuditRun auditRun = new AuditRun(
                auditId,
                projectId,
                Instant.parse("2026-11-22T09:41:00Z"),
                new AuditReport("/workspace/project", 1, 0, true, java.util.List.of()));
        given(runProjectAuditUseCase.findByProjectAndId(projectId, auditId))
                .willReturn(auditRun);

        mockMvc.perform(get("/api/v1/projects/{id}/audits/{auditId}", projectId, auditId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(auditId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()));
    }

    @Test
    void configuresRepositorySource() throws Exception {
        UUID projectId = UUID.randomUUID();
        RepositorySource source = new RepositorySource(
                UUID.randomUUID(),
                projectId,
                "https://github.com/example/as-sentinel",
                "main",
                "/workspace/as-sentinel",
                RepositorySourceStatus.CONFIGURED,
                Instant.now());
        given(repositorySourceUseCase.configure(
                projectId,
                "https://github.com/example/as-sentinel",
                "main",
                "/workspace/as-sentinel"))
                .willReturn(source);

        mockMvc.perform(put("/api/v1/projects/{id}/repository-source", projectId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "repositoryUrl": "https://github.com/example/as-sentinel",
                                  "branch": "main",
                                  "localDirectory": "/workspace/as-sentinel"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryUrl")
                        .value("https://github.com/example/as-sentinel"))
                .andExpect(jsonPath("$.status").value("CONFIGURED"));
    }

    @Test
    void configuresScanConfig() throws Exception {
        UUID projectId = UUID.randomUUID();
        ScanConfig config = new ScanConfig(
                UUID.randomUUID(),
                projectId,
                java.util.List.of(".java", ".py"),
                java.util.List.of(".git", "target"),
                5,
                0.45,
                Instant.now());
        given(scanConfigUseCase.configure(
                projectId,
                java.util.List.of(".java", ".py"),
                java.util.List.of(".git", "target"),
                5,
                0.45))
                .willReturn(config);

        mockMvc.perform(put("/api/v1/projects/{id}/scan-config", projectId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "extensions": [".java", ".py"],
                                  "exclusions": [".git", "target"],
                                  "topKRules": 5,
                                  "scoreThreshold": 0.45
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topKRules").value(5))
                .andExpect(jsonPath("$.scoreThreshold").value(0.45))
                .andExpect(jsonPath("$.extensions").isArray());
    }

    @Test
    void runsConfiguredAuditForProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        AuditRun auditRun = new AuditRun(
                UUID.randomUUID(),
                projectId,
                Instant.now(),
                new AuditReport(
                        "/workspace/as-sentinel",
                        1,
                        0,
                        true,
                        java.util.List.of()));
        given(runProjectAuditUseCase.executeWithConfiguration(projectId))
                .willReturn(auditRun);

        mockMvc.perform(post("/api/v1/projects/{id}/audits/configured", projectId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.report.directoryPath")
                        .value("/workspace/as-sentinel"));
    }
}
