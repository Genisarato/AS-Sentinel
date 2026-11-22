package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.RepositorySourceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repository_sources")
public class RepositorySourceEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private ProjectEntity project;

    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;

    @Column(nullable = false)
    private String branch;

    @Column(name = "local_directory")
    private String localDirectory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositorySourceStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RepositorySourceEntity() {
    }

    public RepositorySourceEntity(
        UUID id,
        ProjectEntity project,
        String repositoryUrl,
        String branch,
        String localDirectory,
        RepositorySourceStatus status,
        Instant updatedAt
    ) {
        this.id = id;
        this.project = project;
        this.repositoryUrl = repositoryUrl;
        this.branch = branch;
        this.localDirectory = localDirectory;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getBranch() {
        return branch;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public RepositorySourceStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
