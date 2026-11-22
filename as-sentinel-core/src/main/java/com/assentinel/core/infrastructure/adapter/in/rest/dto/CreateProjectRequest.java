package com.assentinel.core.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
    @NotBlank(message = "Project name must not be blank")
    String name,

    @NotBlank(message = "Repository URL must not be blank")
    String repositoryUrl
) {
}
