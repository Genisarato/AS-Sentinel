package com.assentinel.core.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "audit_rule_violations")
public class RuleViolationEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finding_id", nullable = false)
    private FindingEntity finding;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(nullable = false, columnDefinition = "text")
    private String suggestion;

    protected RuleViolationEntity() {
    }

    public RuleViolationEntity(
        UUID id,
        String ruleId,
        String severity,
        String reason,
        String suggestion
    ) {
        this.id = id;
        this.ruleId = ruleId;
        this.severity = severity;
        this.reason = reason;
        this.suggestion = suggestion;
    }

    void setFinding(FindingEntity finding) {
        this.finding = finding;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getSeverity() {
        return severity;
    }

    public String getReason() {
        return reason;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
