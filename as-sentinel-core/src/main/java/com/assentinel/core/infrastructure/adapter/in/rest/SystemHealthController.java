package com.assentinel.core.infrastructure.adapter.in.rest;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;
import com.assentinel.core.domain.port.out.ArchitectureRulesPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemHealthController {

    private final ArchitectureRulesPort architectureRulesPort;

    public SystemHealthController(ArchitectureRulesPort architectureRulesPort) {
        this.architectureRulesPort = architectureRulesPort;
    }

    @GetMapping("/ai-health")
    public ResponseEntity<Map<String, String>> aiHealth() {
        QdrantHealth health = architectureRulesPort.getQdrantHealth();
        String status = "connected".equalsIgnoreCase(health.qdrantStatus()) ? "UP" : "DOWN";

        return ResponseEntity.ok(Map.of("ai_service", status));
    }

    @PostMapping("/init-rules-store")
    public ResponseEntity<Map<String, Object>> initializeRulesStore() {
        CollectionInitialization initialization = architectureRulesPort.initializeRulesCollection();
        boolean initialized = "created".equalsIgnoreCase(initialization.status())
                || "already_exists".equalsIgnoreCase(initialization.status());

        return ResponseEntity.ok(Map.of(
                "initialized", initialized,
                "collection", initialization.collection()
        ));
    }
}
