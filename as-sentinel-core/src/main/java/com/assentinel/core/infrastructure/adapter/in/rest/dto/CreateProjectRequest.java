package com.assentinel.core.infrastructure.adapter.in.rest.dto;

public record CreateProjectRequest(
    String name,
    String repositoryUrl
) {
}
