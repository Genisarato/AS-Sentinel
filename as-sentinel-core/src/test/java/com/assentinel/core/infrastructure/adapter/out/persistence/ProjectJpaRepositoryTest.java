package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProjectJpaRepositoryTest {

    @Autowired
    private JpaProjectRepository projectRepository;

    @Test
    void createsAndFindsProject() {
        UUID id = UUID.randomUUID();
        ProjectEntity saved = projectRepository.save(new ProjectEntity(
                id,
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.parse("2026-11-22T09:41:00Z")));

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(projectRepository.findById(id)).isPresent();
        assertThat(projectRepository.findAll())
                .extracting(ProjectEntity::getName)
                .containsExactly("AS-Sentinel");
    }
}
