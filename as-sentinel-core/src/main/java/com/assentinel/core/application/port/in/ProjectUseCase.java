package com.assentinel.core.application.port.in;

import com.assentinel.core.domain.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectUseCase {
    Project create(String name, String repositoryUrl);
    Optional<Project> findById(UUID id);
    List<Project> findAll();
}
