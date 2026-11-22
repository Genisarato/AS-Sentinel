package com.assentinel.core.infrastructure.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "scan_configs")
public class ScanConfigEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private ProjectEntity project;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scan_config_extensions", joinColumns = @JoinColumn(name = "scan_config_id"))
    @Column(name = "extension", nullable = false)
    private Set<String> extensions = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scan_config_exclusions", joinColumns = @JoinColumn(name = "scan_config_id"))
    @Column(name = "exclusion", nullable = false)
    private Set<String> exclusions = new LinkedHashSet<>();

    @Column(name = "top_k_rules", nullable = false)
    private int topKRules;

    @Column(name = "score_threshold", nullable = false)
    private double scoreThreshold;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ScanConfigEntity() {
    }

    public ScanConfigEntity(
        UUID id,
        ProjectEntity project,
        List<String> extensions,
        List<String> exclusions,
        int topKRules,
        double scoreThreshold,
        Instant updatedAt
    ) {
        this.id = id;
        this.project = project;
        this.extensions = new LinkedHashSet<>(extensions);
        this.exclusions = new LinkedHashSet<>(exclusions);
        this.topKRules = topKRules;
        this.scoreThreshold = scoreThreshold;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    public Set<String> getExclusions() {
        return exclusions;
    }

    public int getTopKRules() {
        return topKRules;
    }

    public double getScoreThreshold() {
        return scoreThreshold;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
