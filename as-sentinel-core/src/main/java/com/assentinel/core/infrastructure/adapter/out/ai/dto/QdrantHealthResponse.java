package com.assentinel.core.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record QdrantHealthResponse(
    @JsonProperty("qdrant_status") String qdrantStatus,
    @JsonProperty("existing_collections") List<String> existingCollections
) {}