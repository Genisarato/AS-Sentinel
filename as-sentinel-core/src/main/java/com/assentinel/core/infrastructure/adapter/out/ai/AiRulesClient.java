package com.assentinel.core.infrastructure.adapter.out.ai;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.CollectionInitializationResponse;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.QdrantHealthResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiRulesClient implements ArchitectureRulesPort {

    private final RestClient aiRestClient;

    public AiRulesClient(RestClient aiRestClient) {
        this.aiRestClient = aiRestClient;
    }

    @Override
    public QdrantHealth getQdrantHealth() {
        QdrantHealthResponse response = responseOrThrow(aiRestClient.get()
                .uri("/api/v1/rules/health/qdrant")
                .retrieve()
                .body(QdrantHealthResponse.class), "Qdrant health");

        return new QdrantHealth(response.qdrantStatus(), response.existingCollections());
    }

    @Override
    public CollectionInitialization initializeRulesCollection() {
        CollectionInitializationResponse response = responseOrThrow(aiRestClient.post()
                .uri("/api/v1/rules/init-collection")
                .retrieve()
                .body(CollectionInitializationResponse.class), "collection initialization");

        return new CollectionInitialization(response.status(), response.collection());
    }

    private static <T> T responseOrThrow(T response, String operation) {
        if (response == null) {
            throw new IllegalStateException("AI service returned an empty " + operation + " response");
        }
        return response;
    }
}