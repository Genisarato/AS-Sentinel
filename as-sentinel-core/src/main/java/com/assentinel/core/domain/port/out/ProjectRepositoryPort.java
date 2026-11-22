package com.assentinel.core.domain.port.out;

import com.assentinel.core.domain.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepositoryPort {
    Project save(Project project);
    Optional<Project> findById(UUID id);
    List<Project> findAll();
}
