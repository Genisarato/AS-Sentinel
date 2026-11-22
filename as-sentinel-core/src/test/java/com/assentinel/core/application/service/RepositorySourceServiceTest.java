package com.assentinel.core.application.service;

import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.model.RepositorySource;
import com.assentinel.core.domain.model.RepositorySourceStatus;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import com.assentinel.core.domain.port.out.RepositorySourceRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RepositorySourceServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final RepositorySourceRepositoryPort sourceRepository =
            mock(RepositorySourceRepositoryPort.class);
    private final RepositorySourceService service =
            new RepositorySourceService(projectRepository, sourceRepository);

    @Test
    void configuresRepositorySourceForExistingProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project(
                projectId,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now());
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(sourceRepository.save(any(RepositorySource.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RepositorySource source = service.configure(
                projectId,
                "https://github.com/example/as-sentinel",
                "develop",
                "/workspace/as-sentinel");

        assertThat(source.projectId()).isEqualTo(projectId);
        assertThat(source.branch()).isEqualTo("develop");
        assertThat(source.status()).isEqualTo(RepositorySourceStatus.CONFIGURED);
        verify(sourceRepository).save(source);
    }

    @Test
    void rejectsRepositorySourceForMissingProject() {
        UUID projectId = UUID.randomUUID();
        given(projectRepository.findById(projectId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.configure(
                projectId,
                "https://github.com/example/as-sentinel",
                "main",
                null))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void updatesRepositorySourceStatus() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project(
                projectId,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now());
        RepositorySource source = new RepositorySource(
                UUID.randomUUID(),
                projectId,
                "https://github.com/example/as-sentinel",
                "main",
                null,
                RepositorySourceStatus.CONFIGURED,
                Instant.now());
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(sourceRepository.findByProjectId(projectId)).willReturn(Optional.of(source));
        given(sourceRepository.save(any(RepositorySource.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RepositorySource updated =
                service.updateStatus(projectId, RepositorySourceStatus.CLONED);

        assertThat(updated.status()).isEqualTo(RepositorySourceStatus.CLONED);
    }
}
