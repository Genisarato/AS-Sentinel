package com.assentinel.core.application.port.in;

import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;

public interface AuditProjectUseCase {
    AuditReport execute(AuditRequest request);
}
