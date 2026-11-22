package com.assentinel.core.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuditRunJpaRepositoryTest {

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaAuditRunRepository auditRunRepository;

    @Test
    void persistsRunFindingsAndViolationsThroughRelations() {
        ProjectEntity project = projectRepository.save(new ProjectEntity(
                UUID.randomUUID(),
                "AS-Sentinel",
                "https://github.com/example/as-sentinel",
                Instant.now()));

        AuditRunEntity run = new AuditRunEntity(
                UUID.randomUUID(),
                project,
                Instant.now(),
                "/workspace/project",
                1,
                1,
                false);
        FindingEntity finding = new FindingEntity(
                UUID.randomUUID(),
                "src/main/java/Bad.java",
                false);
        finding.addViolation(new RuleViolationEntity(
                UUID.randomUUID(),
                "ADR-0002",
                "error",
                "Wrong package",
                "Move it"));
        run.addFinding(finding);

        AuditRunEntity saved = auditRunRepository.save(run);

        then(saved.getFindings()).hasSize(1);
        FindingEntity savedFinding = saved.getFindings().iterator().next();
        then(savedFinding.getViolations()).hasSize(1);
        then(savedFinding.getViolations().iterator().next().getRuleId())
                .isEqualTo("ADR-0002");
        then(auditRunRepository.findByProject_IdOrderByCreatedAtDesc(project.getId()))
                .hasSize(1);
    }
}
