package com.assentinel.core.infrastructure.adapter.in.rest;

import com.assentinel.core.application.port.in.ProjectUseCase;
import com.assentinel.core.application.port.in.RunProjectAuditUseCase;
import com.assentinel.core.application.port.in.RepositorySourceUseCase;
import com.assentinel.core.application.port.in.ScanConfigUseCase;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditRun;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.AuditProjectRequest;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.CreateProjectRequest;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.ConfigureRepositorySourceRequest;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.UpdateRepositorySourceStatusRequest;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.ConfigureScanConfigRequest;
import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.model.ScanConfig;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectUseCase projectUseCase;
    private final RunProjectAuditUseCase runProjectAuditUseCase;
    private final RepositorySourceUseCase repositorySourceUseCase;
    private final ScanConfigUseCase scanConfigUseCase;

    public ProjectController(
        ProjectUseCase projectUseCase,
        RunProjectAuditUseCase runProjectAuditUseCase,
        RepositorySourceUseCase repositorySourceUseCase,
        ScanConfigUseCase scanConfigUseCase
    ) {
        this.projectUseCase = projectUseCase;
        this.runProjectAuditUseCase = runProjectAuditUseCase;
        this.repositorySourceUseCase = repositorySourceUseCase;
        this.scanConfigUseCase = scanConfigUseCase;
    }

    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody CreateProjectRequest request) {
        Project project = projectUseCase.create(request.name(), request.repositoryUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping
    public List<Project> findAll() {
        return projectUseCase.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findById(@PathVariable UUID id) {
        return projectUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/audits")
    public ResponseEntity<AuditRun> runAudit(
        @PathVariable UUID id,
        @Valid @RequestBody AuditProjectRequest request
    ) {
        AuditRequest auditRequest = request.topKRules() == null || request.scoreThreshold() == null
                ? new AuditRequest(request.directoryPath())
                : new AuditRequest(
                        request.directoryPath(),
                        request.topKRules(),
                        request.scoreThreshold());

        AuditRun auditRun = runProjectAuditUseCase.execute(id, auditRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(auditRun);
    }

    @PostMapping("/{id}/audits/configured")
    public ResponseEntity<AuditRun> runConfiguredAudit(@PathVariable UUID id) {
        AuditRun auditRun = runProjectAuditUseCase.executeWithConfiguration(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(auditRun);
    }

    @GetMapping("/{id}/audits")
    public List<AuditRun> findAudits(@PathVariable UUID id) {
        return runProjectAuditUseCase.findByProjectId(id);
    }

    @GetMapping("/{id}/audits/{auditId}")
    public AuditRun findAudit(
        @PathVariable UUID id,
        @PathVariable UUID auditId
    ) {
        return runProjectAuditUseCase.findByProjectAndId(id, auditId);
    }

    @PutMapping("/{id}/repository-source")
    public RepositorySource configureRepositorySource(
        @PathVariable UUID id,
        @Valid @RequestBody ConfigureRepositorySourceRequest request
    ) {
        return repositorySourceUseCase.configure(
                id,
                request.repositoryUrl(),
                request.branch(),
                request.localDirectory());
    }

    @GetMapping("/{id}/repository-source")
    public ResponseEntity<RepositorySource> findRepositorySource(@PathVariable UUID id) {
        return repositorySourceUseCase.findByProjectId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/repository-source/status")
    public RepositorySource updateRepositorySourceStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateRepositorySourceStatusRequest request
    ) {
        return repositorySourceUseCase.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}/repository-source")
    public ResponseEntity<Void> deleteRepositorySource(@PathVariable UUID id) {
        repositorySourceUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/scan-config")
    public ScanConfig configureScanConfig(
        @PathVariable UUID id,
        @Valid @RequestBody ConfigureScanConfigRequest request
    ) {
        return scanConfigUseCase.configure(
                id,
                request.extensions(),
                request.exclusions(),
                request.topKRules(),
                request.scoreThreshold());
    }

    @GetMapping("/{id}/scan-config")
    public ResponseEntity<ScanConfig> findScanConfig(@PathVariable UUID id) {
        return scanConfigUseCase.findByProjectId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/scan-config")
    public ResponseEntity<Void> deleteScanConfig(@PathVariable UUID id) {
        scanConfigUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
