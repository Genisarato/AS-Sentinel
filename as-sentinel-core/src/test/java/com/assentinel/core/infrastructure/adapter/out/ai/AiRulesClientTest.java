package com.assentinel.core.infrastructure.adapter.out.ai;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditReport;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiRulesClientTest {

    private final RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("http://localhost:8000");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final AiRulesClient client = new AiRulesClient(restClientBuilder.build());

    @Test
    void auditsProjectThroughAiApi() {
        server.expect(requestTo("http://localhost:8000/api/v1/audit/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "directory_path": "/workspace/project",
                          "top_k_rules": 3,
                          "score_threshold": 0.35
                        }
                        """))
                .andRespond(withSuccess("""
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
                        """, MediaType.APPLICATION_JSON));

        AuditReport response = client.auditProject(
                new AuditRequest("/workspace/project"));

        assertThat(response.directoryPath()).isEqualTo("/workspace/project");
        assertThat(response.totalViolations()).isEqualTo(1);
        assertThat(response.projectCompliant()).isFalse();
        server.verify();
    }

    @Test
    void getsQdrantHealth() {
        server.expect(requestTo("http://localhost:8000/api/v1/rules/health/qdrant"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "qdrant_status": "connected",
                          "existing_collections": ["architecture_rules"]
                        }
                        """, MediaType.APPLICATION_JSON));

        QdrantHealth response = client.getQdrantHealth();

        assertThat(response.qdrantStatus()).isEqualTo("connected");
        assertThat(response.existingCollections()).containsExactly("architecture_rules");
        server.verify();
    }

    @Test
    void initializesRulesCollection() {
        server.expect(requestTo("http://localhost:8000/api/v1/rules/init-collection"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "status": "already_exists",
                          "collection": "architecture_rules"
                        }
                        """, MediaType.APPLICATION_JSON));

        CollectionInitialization response = client.initializeRulesCollection();

        assertThat(response.status()).isEqualTo("already_exists");
        assertThat(response.collection()).isEqualTo("architecture_rules");
        server.verify();
    }
}