package com.assentinel.core.infrastructure.adapter.in.web;

import com.assentinel.core.application.port.in.ProjectUseCase;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.infrastructure.adapter.in.rest.ProjectController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectUseCase projectUseCase;

    @Test
    void createsProject() throws Exception {
        UUID id = UUID.randomUUID();
        given(projectUseCase.create("AS-Sentinel", "https://github.com/example/as-sentinel"))
                .willReturn(new Project(
                        id,
                        "AS-Sentinel",
                        "https://github.com/example/as-sentinel",
                        Instant.parse("2026-11-22T09:41:00Z")));

        mockMvc.perform(post("/api/v1/projects")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "AS-Sentinel",
                                  "repositoryUrl": "https://github.com/example/as-sentinel"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("AS-Sentinel"));
    }
}
