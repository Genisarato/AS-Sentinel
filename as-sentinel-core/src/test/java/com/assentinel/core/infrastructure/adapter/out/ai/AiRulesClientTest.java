package com.assentinel.core.infrastructure.adapter.out.ai;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiRulesClientTest {

    private final RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("http://localhost:8000");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final AiRulesClient client = new AiRulesClient(restClientBuilder.build());

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