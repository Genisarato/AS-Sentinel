package com.assentinel.core.application.service;

import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditProjectServiceTest {

    private final ArchitectureRulesPort architectureRulesPort = mock(ArchitectureRulesPort.class);
    private final AuditProjectService service = new AuditProjectService(architectureRulesPort);

    @Test
    void executesProjectAuditThroughOutboundPort() {
        AuditRequest request = new AuditRequest("/workspace/project");
        AuditReport report = new AuditReport(
                "/workspace/project",
                1,
                0,
                true,
                List.of());
        given(architectureRulesPort.auditProject(request)).willReturn(report);

        AuditReport result = service.execute(request);

        assertThat(result).isSameAs(report);
        verify(architectureRulesPort).auditProject(request);
    }
}
