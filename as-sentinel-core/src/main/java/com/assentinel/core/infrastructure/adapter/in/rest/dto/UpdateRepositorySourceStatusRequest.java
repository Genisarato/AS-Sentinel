package com.assentinel.core.infrastructure.adapter.in.rest.dto;

import com.assentinel.core.domain.model.RepositorySourceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRepositorySourceStatusRequest(
    @NotNull(message = "Repository source status must not be null")
    RepositorySourceStatus status
) {
}
