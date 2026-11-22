package com.assentinel.core.infrastructure.adapter.in.web;

import com.assentinel.core.application.port.in.AuditProjectUseCase;
import com.assentinel.core.application.exception.AiServiceUnavailableException;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.infrastructure.adapter.in.rest.AuditController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditProjectUseCase auditProjectUseCase;

    @Test
    void runsAuditWithDefaultOptions() throws Exception {
        given(auditProjectUseCase.execute(org.mockito.ArgumentMatchers.any(AuditRequest.class)))
                .willReturn(new AuditReport(
                        "/workspace/project",
                        1,
                        0,
                        true,
                        List.of()));

        mockMvc.perform(post("/api/v1/audits")
                        .contentType("application/json")
                        .content("""
                                {
                                  "directoryPath": "/workspace/project"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.directoryPath").value("/workspace/project"))
                .andExpect(jsonPath("$.projectCompliant").value(true));

        ArgumentCaptor<AuditRequest> captor = ArgumentCaptor.forClass(AuditRequest.class);
        verify(auditProjectUseCase).execute(captor.capture());
        assertThat(captor.getValue().topKRules()).isEqualTo(3);
        assertThat(captor.getValue().scoreThreshold()).isEqualTo(0.35);
    }

    @Test
    void rejectsInvalidAuditOptions() throws Exception {
        mockMvc.perform(post("/api/v1/audits")
                        .contentType("application/json")
                        .content("""
                                {
                                  "directoryPath": "/workspace/project",
                                  "topKRules": 0,
                                  "scoreThreshold": 1.5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returnsServiceUnavailableWhenAiIsDown() throws Exception {
        given(auditProjectUseCase.execute(org.mockito.ArgumentMatchers.any(AuditRequest.class)))
                .willThrow(new AiServiceUnavailableException());

        mockMvc.perform(post("/api/v1/audits")
                        .contentType("application/json")
                        .content("""
                                {
                                  "directoryPath": "/workspace/project"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("The AI service is unavailable"));
    }
}
