package com.infsus.dz3_md.repository;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.domain.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectRepository repo;

    private Project p1, p2;

    private final Pageable pageable = PageRequest.of(0, 10);

    /**
     * Persist a new Project with all required fields set.
     */
    private Project persistProject(String name) {
        Project p = new Project();
        p.setName(name);
        p.setStatus(Status.PLANNED);            // required
        p.setAverageTotalAssets(1);             // must be > 0
        em.persist(p);
        return p;
    }

    @BeforeEach
    void setUp() {
        p1 = persistProject("First Project");
        p2 = persistProject("Second Project");
        em.flush();  // make sure everything is written before queries
    }

    @Test
    void findAll_withPageable_returnsBoth() {
        Page<Project> page = repo.findAll(pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(Project::getName)
                .containsExactlyInAnyOrder("First Project", "Second Project");
    }

    @Test
    void findById_returnsCorrectEntity() {
        Project found = repo.findById(p1.getProjectId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("First Project");
    }
}
