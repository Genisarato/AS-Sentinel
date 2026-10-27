package com.assentinel.core.domain.model;

import java.util.List;

public record QdrantHealth(
    String qdrantStatus,
    List<String> existingCollections
) {}