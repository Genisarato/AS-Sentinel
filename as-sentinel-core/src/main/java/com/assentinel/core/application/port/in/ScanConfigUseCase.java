package com.assentinel.core.application.port.in;

import com.assentinel.core.domain.model.ScanConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanConfigUseCase {
    ScanConfig configure(
        UUID projectId,
        List<String> extensions,
        List<String> exclusions,
        int topKRules,
        double scoreThreshold
    );
    Optional<ScanConfig> findByProjectId(UUID projectId);
    void delete(UUID projectId);
}
