package com.infsus.dz3_md.integration;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.domain.Status;
import com.infsus.dz3_md.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void clean() {
        projectRepository.deleteAll();
    }


    @BeforeEach
    void cleanAndSeed() {
        projectRepository.deleteAll();
    }

    @Test
    void fullCrudFlow() throws Exception {
        // CREATE
        mvc.perform(post("/projects")
                        .param("name", "XProj")
                        .param("status", "ACTIVE")
                        .param("mission", "M")
                        .param("vision", "V")
                        .param("description", "D")
                        .param("averageTotalAssets", "123")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));


        assertThat(projectRepository.count()).isOne();

        Project p = projectRepository.findAll().get(0);
        UUID id = p.getProjectId();
        assertThat(p.getName()).isEqualTo("XProj");

        // READ (list)
        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attributeExists("page"));

        // READ (edit form)
        mvc.perform(get("/projects/{id}", p.getProjectId()))
                .andExpect(status().isOk())
                .andExpect(view().name("project-form"))
                .andExpect(model().attribute("project",
                        hasProperty("projectId", equalTo(p.getProjectId()))));

        // UPDATE
        mvc.perform(post("/projects/{id}", id)
                        .param("name", "XProjUpdated")
                        .param("status", "ACTIVE")
                        .param("mission", "M2")
                        .param("vision", "V2")
                        .param("description", "D2")
                        .param("averageTotalAssets", "456")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Project updated successfully!"))
                .andExpect(redirectedUrl("/projects"));

        assertThat(projectRepository.findById(id).get().getName())
                .isEqualTo("XProjUpdated");

        // DELETE
        mvc.perform(post("/projects/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Project deleted successfully!"))
                .andExpect(redirectedUrl("/projects"));

        assertThat(projectRepository.existsById(id)).isFalse();
    }
}
