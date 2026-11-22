package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProjectRepositoryAdapter implements ProjectRepositoryPort {

    private final JpaProjectRepository repository;

    public ProjectRepositoryAdapter(JpaProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = repository.save(toEntity(project));
        return toDomain(entity);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private ProjectEntity toEntity(Project project) {
        return new ProjectEntity(
                project.id(),
                project.name(),
                project.repositoryUrl(),
                project.createdAt());
    }

    private Project toDomain(ProjectEntity entity) {
        return new Project(
                entity.getId(),
                entity.getName(),
                entity.getRepositoryUrl(),
                entity.getCreatedAt());
    }
}
