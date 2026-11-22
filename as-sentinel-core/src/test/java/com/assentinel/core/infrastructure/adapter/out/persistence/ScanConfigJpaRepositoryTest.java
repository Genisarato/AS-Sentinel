package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ScanConfigJpaRepositoryTest {

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaScanConfigRepository scanConfigRepository;

    @Test
    void persistsScanConfigCollectionsForProject() {
        ProjectEntity project = projectRepository.save(new ProjectEntity(
                UUID.randomUUID(),
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now()));

        ScanConfigEntity saved = scanConfigRepository.save(new ScanConfigEntity(
                UUID.randomUUID(),
                project,
                List.of(".java", ".py"),
                List.of(".git", "target"),
                5,
                0.45,
                Instant.now()));

        assertThat(scanConfigRepository.findByProject_Id(project.getId())).isPresent();
        assertThat(saved.getExtensions()).containsExactly(".java", ".py");
        assertThat(saved.getExclusions()).containsExactly(".git", "target");
        assertThat(saved.getTopKRules()).isEqualTo(5);
    }
}
