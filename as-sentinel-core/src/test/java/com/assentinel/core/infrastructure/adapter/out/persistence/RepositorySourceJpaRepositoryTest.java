package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.RepositorySourceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RepositorySourceJpaRepositoryTest {

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaRepositorySourceRepository sourceRepository;

    @Test
    void persistsOneRepositorySourcePerProject() {
        ProjectEntity project = projectRepository.save(new ProjectEntity(
                UUID.randomUUID(),
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now()));

        RepositorySourceEntity source = sourceRepository.save(
                new RepositorySourceEntity(
                        UUID.randomUUID(),
                        project,
                        "https://github.com/example/as-sentinel",
                        "main",
                        "/workspace/as-sentinel",
                        RepositorySourceStatus.CONFIGURED,
                        Instant.now()));

        assertThat(sourceRepository.findByProject_Id(project.getId())).isPresent();
        assertThat(sourceRepository.findByProject_Id(project.getId()).orElseThrow()
                .getBranch()).isEqualTo("main");
    }
}
