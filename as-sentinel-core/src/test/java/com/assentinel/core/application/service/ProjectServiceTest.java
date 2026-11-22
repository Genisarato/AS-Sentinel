package com.assentinel.core.application.service;

import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProjectServiceTest {

    private final ProjectRepositoryPort repository = mock(ProjectRepositoryPort.class);
    private final ProjectService service = new ProjectService(repository);

    @Test
    void createsProjectWithGeneratedIdentityAndTimestamp() {
        given(repository.save(any(Project.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Project project = service.create("AS-Sentinel", "https://github.com/example/as-sentinel");

        assertThat(project.id()).isNotNull();
        assertThat(project.createdAt()).isBeforeOrEqualTo(Instant.now());
        verify(repository).save(project);
    }
}
