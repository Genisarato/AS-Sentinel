package com.assentinel.core.infrastructure.adapter.in.web;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import com.assentinel.core.infrastructure.adapter.in.rest.SystemHealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemHealthController.class)
class SystemHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArchitectureRulesPort architectureRulesPort;

    @Test
    void shouldReturnUpStatusWhenAiIsHealthy() throws Exception {
        given(architectureRulesPort.getQdrantHealth())
                .willReturn(new QdrantHealth("connected", List.of("architecture_rules")));

        mockMvc.perform(get("/api/v1/system/ai-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ai_service").value("UP"));
    }

    @Test
    void shouldReturnInitializedStatusWhenCollectionIsInit() throws Exception {
        given(architectureRulesPort.initializeRulesCollection())
                .willReturn(new CollectionInitialization("created", "architecture_rules"));

        mockMvc.perform(post("/api/v1/system/init-rules-store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialized").value(true));
    }
}