package com.assentinel.core.domain.port.out;

import com.assentinel.core.domain.model.CollectionInitialization;
import com.assentinel.core.domain.model.QdrantHealth;

public interface ArchitectureRulesPort {
    QdrantHealth getQdrantHealth();
    CollectionInitialization initializeRulesCollection();
}