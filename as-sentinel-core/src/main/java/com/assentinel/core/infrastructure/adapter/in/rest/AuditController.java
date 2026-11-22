package com.assentinel.core.infrastructure.adapter.in.rest;

import com.assentinel.core.application.port.in.AuditProjectUseCase;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.AuditProjectRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audits")
public class AuditController {

    private final AuditProjectUseCase auditProjectUseCase;

    public AuditController(AuditProjectUseCase auditProjectUseCase) {
        this.auditProjectUseCase = auditProjectUseCase;
    }

    @PostMapping
    public ResponseEntity<AuditReport> auditProject(@RequestBody AuditProjectRequest request) {
        AuditRequest auditRequest = request.topKRules() == null || request.scoreThreshold() == null
                ? new AuditRequest(request.directoryPath())
                : new AuditRequest(
                        request.directoryPath(),
                        request.topKRules(),
                        request.scoreThreshold());

        return ResponseEntity.ok(auditProjectUseCase.execute(auditRequest));
    }
}
