package com.infsus.dz3_md.integration;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.domain.Status;
import com.infsus.dz3_md.repository.ProductRepository;
import com.infsus.dz3_md.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ProductIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ProjectRepository projectRepo;
    @Autowired ProductRepository productRepo;

    private Project proj;
    private Product existing;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        projectRepo.deleteAll();
        productRepo.deleteAll();
        proj = new Project();
        proj.setName("P1");
        proj.setStatus(Status.ACTIVE);
        proj.setAverageTotalAssets(50);
        proj = projectRepo.save(proj);

        existing = new Product();
        existing.setName("ProdX");
        existing.setPrice(new BigDecimal("9.99"));
        existing.setDescription("Desc");
        existing.setProject(proj);
        existing = productRepo.save(existing);
    }

    @Test
    void fullCrudFlow() throws Exception {
        mvc.perform(post("/products")
                        .param("name", "NewProd")
                        .param("price", "5.00")
                        .param("description", "D")
                        .param("project.projectId", proj.getProjectId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/projects/*"));

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", equalTo(2L))));

        mvc.perform(get("/products/{id}", existing.getProductId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attribute("product",
                        hasProperty("productId", equalTo(existing.getProductId()))))
                .andExpect(model().attribute("allProjects",
                        hasSize(1)));

        mvc.perform(post("/products")
                        .param("productId", existing.getProductId().toString())
                        .param("name", "ProdXUpdated")
                        .param("price", "19.99")
                        .param("description", "DescU")
                        .param("project.projectId", proj.getProjectId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/" + proj.getProjectId()));

        mvc.perform(get("/products/{id}", existing.getProductId()))
                .andExpect(model().attribute("product",
                        hasProperty("name", equalTo("ProdXUpdated"))))
                .andExpect(model().attribute("product",
                        hasProperty("price", equalTo(new BigDecimal("19.99")))));

        System.out.println("We have: " + productRepository.findAll().size());
        mvc.perform(post("/products/{id}/delete", existing.getProductId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/" + proj.getProjectId()));

        mvc.perform(get("/products"))
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", equalTo(1L))));
    }
}
