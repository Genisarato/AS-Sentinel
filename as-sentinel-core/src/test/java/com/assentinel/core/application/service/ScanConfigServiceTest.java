package com.assentinel.core.application.service;

import com.assentinel.core.application.exception.ProjectNotFoundException;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.model.ScanConfig;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import com.assentinel.core.domain.port.out.ScanConfigRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScanConfigServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final ScanConfigRepositoryPort configRepository = mock(ScanConfigRepositoryPort.class);
    private final ScanConfigService service =
            new ScanConfigService(projectRepository, configRepository);

    @Test
    void configuresScanSettingsForExistingProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project(
                projectId,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now());
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(configRepository.findByProjectId(projectId)).willReturn(Optional.empty());
        given(configRepository.save(any(ScanConfig.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ScanConfig config = service.configure(
                projectId,
                List.of(".java", ".py"),
                List.of(".git", "target"),
                5,
                0.45);

        assertThat(config.projectId()).isEqualTo(projectId);
        assertThat(config.extensions()).containsExactly(".java", ".py");
        assertThat(config.exclusions()).containsExactly(".git", "target");
        assertThat(config.topKRules()).isEqualTo(5);
        assertThat(config.scoreThreshold()).isEqualTo(0.45);
        verify(configRepository).save(config);
    }

    @Test
    void rejectsScanConfigForMissingProject() {
        UUID projectId = UUID.randomUUID();
        given(projectRepository.findById(projectId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.configure(
                projectId,
                List.of(".java"),
                List.of(),
                3,
                0.35))
                .isInstanceOf(ProjectNotFoundException.class);
    }
}
