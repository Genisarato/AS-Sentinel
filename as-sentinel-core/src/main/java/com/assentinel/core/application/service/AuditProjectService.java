package com.assentinel.core.application.service;

import com.assentinel.core.application.port.in.AuditProjectUseCase;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import org.springframework.stereotype.Service;

@Service
public class AuditProjectService implements AuditProjectUseCase {

    private final ArchitectureRulesPort architectureRulesPort;

    public AuditProjectService(ArchitectureRulesPort architectureRulesPort) {
        this.architectureRulesPort = architectureRulesPort;
    }

    @Override
    public AuditReport execute(AuditRequest request) {
        return architectureRulesPort.auditProject(request);
    }
}
