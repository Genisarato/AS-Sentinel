package com.assentinel.core.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfigureRepositorySourceRequest(
    @NotBlank(message = "Repository URL must not be blank")
    String repositoryUrl,

    @NotBlank(message = "Branch must not be blank")
    String branch,

    String localDirectory
) {
}
