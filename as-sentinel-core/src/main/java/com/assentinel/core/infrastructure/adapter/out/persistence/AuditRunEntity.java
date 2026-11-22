package com.assentinel.core.infrastructure.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "audit_runs")
public class AuditRunEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "directory_path", nullable = false)
    private String directoryPath;

    @Column(name = "total_files_scanned", nullable = false)
    private int totalFilesScanned;

    @Column(name = "total_violations", nullable = false)
    private int totalViolations;

    @Column(name = "project_compliant", nullable = false)
    private boolean projectCompliant;

    @OneToMany(mappedBy = "auditRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FindingEntity> findings = new HashSet<>();

    protected AuditRunEntity() {
    }

    public AuditRunEntity(
        UUID id,
        ProjectEntity project,
        Instant createdAt,
        String directoryPath,
        int totalFilesScanned,
        int totalViolations,
        boolean projectCompliant
    ) {
        this.id = id;
        this.project = project;
        this.createdAt = createdAt;
        this.directoryPath = directoryPath;
        this.totalFilesScanned = totalFilesScanned;
        this.totalViolations = totalViolations;
        this.projectCompliant = projectCompliant;
    }

    public void addFinding(FindingEntity finding) {
        findings.add(finding);
        finding.setAuditRun(this);
    }

    public UUID getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public int getTotalFilesScanned() {
        return totalFilesScanned;
    }

    public int getTotalViolations() {
        return totalViolations;
    }

    public boolean isProjectCompliant() {
        return projectCompliant;
    }

    public Set<FindingEntity> getFindings() {
        return findings;
    }
}
