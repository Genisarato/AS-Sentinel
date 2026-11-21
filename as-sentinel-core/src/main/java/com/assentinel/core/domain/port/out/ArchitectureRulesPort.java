package com.assentinel.core.domain.port.out;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditReport;

public interface ArchitectureRulesPort {
    QdrantHealth getQdrantHealth();
    CollectionInitialization initializeRulesCollection();
    AuditReport auditProject(AuditRequest request);
}