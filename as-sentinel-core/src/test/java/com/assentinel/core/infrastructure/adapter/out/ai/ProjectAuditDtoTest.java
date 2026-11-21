package com.assentinel.core.infrastructure.adapter.out.ai;

import com.assentinel.core.infrastructure.adapter.out.ai.dto.ProjectAuditRequest;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.ProjectAuditResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectAuditDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsAuditResponseJson() throws Exception {
        ProjectAuditResponse response = objectMapper.readValue("""
                {
                  "directory_path": "/workspace/project",
                  "total_files_scanned": 1,
                  "total_violations": 1,
                  "is_project_compliant": false,
                  "results": [{
                    "file_path": "src/main/Main.java",
                    "is_compliant": false,
                    "violations": [{
                      "rule_id": "ADR-0002",
                      "severity": "error",
                      "reason": "The class is in the wrong package",
                      "suggestion": "Move the class to a domain package"
                    }]
                  }]
                }
                """, ProjectAuditResponse.class);

        assertThat(response.directoryPath()).isEqualTo("/workspace/project");
        assertThat(response.totalFilesScanned()).isOne();
        assertThat(response.projectCompliant()).isFalse();
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst().filePath()).isEqualTo("src/main/Main.java");
        assertThat(response.results().getFirst().violations().getFirst().ruleId())
                .isEqualTo("ADR-0002");
    }

    @Test
    void mapsAuditRequestJsonProperties() throws Exception {
        String json = objectMapper.writeValueAsString(new ProjectAuditRequest("/workspace/project"));

        assertThat(json).contains("\"directory_path\":\"/workspace/project\"");
        assertThat(json).contains("\"top_k_rules\":3");
        assertThat(json).contains("\"score_threshold\":0.35");
    }
}
