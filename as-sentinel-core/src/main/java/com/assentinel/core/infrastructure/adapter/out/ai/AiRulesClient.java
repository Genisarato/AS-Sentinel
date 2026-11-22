package com.assentinel.core.infrastructure.adapter.out.ai;

import com.assentinel.core.application.exception.AiServiceUnavailableException;
import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;
import com.assentinel.core.domain.model.AuditRequest;
import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.Finding;
import com.assentinel.core.domain.model.RuleViolation;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.CollectionInitializationResponse;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.ProjectAuditRequest;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.ProjectAuditResponse;
import com.assentinel.core.infrastructure.adapter.out.ai.dto.QdrantHealthResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AiRulesClient implements ArchitectureRulesPort {

    private final RestClient aiRestClient;

    public AiRulesClient(RestClient aiRestClient) {
        this.aiRestClient = aiRestClient;
    }

    @Override
    public QdrantHealth getQdrantHealth() {
        QdrantHealthResponse response = executeAiRequest(
                () -> responseOrThrow(aiRestClient.get()
                        .uri("/api/v1/rules/health/qdrant")
                        .retrieve()
                        .body(QdrantHealthResponse.class), "Qdrant health"),
                "Qdrant health");

        return new QdrantHealth(response.qdrantStatus(), response.existingCollections());
    }

    @Override
    public CollectionInitialization initializeRulesCollection() {
        CollectionInitializationResponse response = executeAiRequest(
                () -> responseOrThrow(aiRestClient.post()
                        .uri("/api/v1/rules/init-collection")
                        .retrieve()
                        .body(CollectionInitializationResponse.class), "collection initialization"),
                "collection initialization");

        return new CollectionInitialization(response.status(), response.collection());
    }

    @Override
    public AuditReport auditProject(AuditRequest request) {
        ProjectAuditRequest aiRequest = new ProjectAuditRequest(
                request.directoryPath(), request.topKRules(), request.scoreThreshold());

        ProjectAuditResponse response = executeAiRequest(
                () -> responseOrThrow(aiRestClient.post()
                        .uri("/api/v1/audit/")
                        .body(aiRequest)
                        .retrieve()
                        .body(ProjectAuditResponse.class), "project audit"),
                "project audit");

        List<Finding> findings = response.results().stream()
                .map(file -> new Finding(
                        file.filePath(),
                        file.compliant(),
                        file.violations().stream()
                                .map(violation -> new RuleViolation(
                                        violation.ruleId(),
                                        violation.severity(),
                                        violation.reason(),
                                        violation.suggestion()))
                                .toList()))
                .toList();

        return new AuditReport(
                response.directoryPath(),
                response.totalFilesScanned(),
                response.totalViolations(),
                response.projectCompliant(),
                findings);
    }

    private static <T> T responseOrThrow(T response, String operation) {
        if (response == null) {
            throw new IllegalStateException("AI service returned an empty " + operation + " response");
        }
        return response;
    }

    private <T> T executeAiRequest(java.util.concurrent.Callable<T> request, String operation) {
        try {
            return request.call();
        } catch (RestClientException | IllegalStateException exception) {
            throw new AiServiceUnavailableException();
        } catch (Exception exception) {
            throw new AiServiceUnavailableException();
        }
    }
}