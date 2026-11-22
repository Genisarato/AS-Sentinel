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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "audit_findings")
public class FindingEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "audit_run_id", nullable = false)
    private AuditRunEntity auditRun;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(nullable = false)
    private boolean compliant;

    @OneToMany(mappedBy = "finding", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RuleViolationEntity> violations = new HashSet<>();

    protected FindingEntity() {
    }

    public FindingEntity(UUID id, String filePath, boolean compliant) {
        this.id = id;
        this.filePath = filePath;
        this.compliant = compliant;
    }

    void setAuditRun(AuditRunEntity auditRun) {
        this.auditRun = auditRun;
    }

    public void addViolation(RuleViolationEntity violation) {
        violations.add(violation);
        violation.setFinding(this);
    }

    public UUID getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public Set<RuleViolationEntity> getViolations() {
        return violations;
    }
}
