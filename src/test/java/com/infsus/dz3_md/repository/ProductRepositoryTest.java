package com.infsus.dz3_md.repository;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.domain.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityManager;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository repo;

    private Project projA;
    private Product prod1, prod2, prodOther;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        projA     = persistProject("Proj A");
        prod1     = persistProduct(projA, "Alpha");
        prod2     = persistProduct(projA, "alphaNumeric");

        Project projB = persistProject("Proj B");
        prodOther    = persistProduct(projB, "Beta");

        em.flush();
        em.clear();
    }

    @Test
    void findByNameContainingIgnoreCase_shouldFindBothAlpha() {
        Page<Product> page = repo.findByNameContainingIgnoreCase("alpha", pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Alpha", "alphaNumeric");
    }

    @Test
    void findByProject_ProjectId_shouldOnlyReturnProjectAProducts() {
        Page<Product> page = repo.findByProject_ProjectId(projA.getProjectId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .allMatch(p -> p.getProject().getProjectId().equals(projA.getProjectId()));
    }

    @Test
    void findByProjectAndNameContainingIgnoreCase_shouldFilterWithinProject() {
        Page<Product> page = repo.findByProject_ProjectIdAndNameContainingIgnoreCase(
                projA.getProjectId(), "numeric", pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("alphaNumeric");
    }

    @Test
    void existsByNameAndProject_ProjectId_andNot() {
        assertThat(repo.existsByNameAndProject_ProjectId("Alpha", projA.getProjectId()))
                .isTrue();

        assertThat(repo.existsByNameAndProject_ProjectIdAndProductIdNot(
                "Alpha", projA.getProjectId(), prod1.getProductId()))
                .isFalse();

        assertThat(repo.existsByNameAndProject_ProjectIdAndProductIdNot(
                "Alpha", projA.getProjectId(), UUID.randomUUID()))
                .isTrue();
    }


    /**
     * Persist a new Project with all required fields set
     */
    private Project persistProject(String name) {
        Project p = new Project();
        p.setName(name);
        p.setStatus(Status.PLANNED);
        p.setAverageTotalAssets(5);
        em.persist(p);
        return p;
    }

    /**
     * Persist a new Product attached to the given project
     */
    private Product persistProduct(Project project, String productName) {
        Product prod = new Product();
        prod.setName(productName);
        prod.setProject(project);
        prod.setPrice(BigDecimal.valueOf(100.0));
        em.persist(prod);
        return prod;
    }
}
