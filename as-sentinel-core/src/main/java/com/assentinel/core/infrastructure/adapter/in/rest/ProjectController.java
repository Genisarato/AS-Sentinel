package com.assentinel.core.infrastructure.adapter.in.rest;

import com.assentinel.core.application.port.in.ProjectUseCase;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.infrastructure.adapter.in.rest.dto.CreateProjectRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectUseCase projectUseCase;

    public ProjectController(ProjectUseCase projectUseCase) {
        this.projectUseCase = projectUseCase;
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody CreateProjectRequest request) {
        Project project = projectUseCase.create(request.name(), request.repositoryUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping
    public List<Project> findAll() {
        return projectUseCase.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findById(@PathVariable UUID id) {
        return projectUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
