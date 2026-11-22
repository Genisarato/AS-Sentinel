package com.assentinel.core.domain.port.out;

import com.assentinel.core.domain.model.ScanConfig;

import java.util.Optional;
import java.util.UUID;

public interface ScanConfigRepositoryPort {
    ScanConfig save(ScanConfig config);
    Optional<ScanConfig> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
