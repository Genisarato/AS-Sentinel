package com.assentinel.core.application.service;

import com.assentinel.core.application.port.in.ProjectUseCase;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService implements ProjectUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;

    public ProjectService(ProjectRepositoryPort projectRepositoryPort) {
        this.projectRepositoryPort = projectRepositoryPort;
    }

    @Override
    public Project create(String name, String repositoryUrl) {
        return projectRepositoryPort.save(
                new Project(UUID.randomUUID(), name, repositoryUrl, Instant.now()));
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return projectRepositoryPort.findById(id);
    }

    @Override
    public List<Project> findAll() {
        return projectRepositoryPort.findAll();
    }
}
