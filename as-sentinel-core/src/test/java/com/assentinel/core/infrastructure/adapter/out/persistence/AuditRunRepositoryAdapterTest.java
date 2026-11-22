package com.assentinel.core.infrastructure.adapter.out.persistence;

import com.assentinel.core.domain.model.AuditReport;
import com.assentinel.core.domain.model.AuditRun;
import com.assentinel.core.domain.model.Finding;
import com.assentinel.core.domain.model.Project;
import com.assentinel.core.domain.model.RuleViolation;
import com.assentinel.core.domain.port.out.AuditRunRepositoryPort;
import com.assentinel.core.domain.port.out.ProjectRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:audit-runs-test",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class AuditRunRepositoryAdapterTest {

    @Autowired
    private ProjectRepositoryPort projectRepository;

    @Autowired
    private AuditRunRepositoryPort auditRunRepository;

    @Test
    void savesAndReloadsAnAuditRunWithFindingsAndViolations() {
        Project project = projectRepository.save(new Project(
                UUID.randomUUID(),
                "Persistence project",
                "https://github.com/example/persistence-project",
                Instant.now()));

        AuditRun auditRun = new AuditRun(
                UUID.randomUUID(),
                project.id(),
                Instant.now(),
                new AuditReport(
                        "/workspace/project",
                        1,
                        1,
                        false,
                        List.of(new Finding(
                                "src/main/java/Bad.java",
                                false,
                                List.of(new RuleViolation(
                                        "ADR-0002",
                                        "error",
                                        "Wrong package",
                                        "Move it to the domain package"))))));

        AuditRun saved = auditRunRepository.save(auditRun);

        assertThat(saved.report().findings()).hasSize(1);
        assertThat(saved.report().findings().getFirst().violations())
                .extracting(RuleViolation::ruleId)
                .containsExactly("ADR-0002");

        List<AuditRun> reloaded = auditRunRepository.findByProjectId(project.id());
        assertThat(reloaded).hasSize(1);
        assertThat(reloaded.getFirst().report().directoryPath())
                .isEqualTo("/workspace/project");
        assertThat(reloaded.getFirst().report().findings().getFirst().violations())
                .extracting(RuleViolation::suggestion)
                .containsExactly("Move it to the domain package");
    }
}
