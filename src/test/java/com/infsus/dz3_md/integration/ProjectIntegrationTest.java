package com.infsus.dz3_md.integration;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.domain.Status;
import com.infsus.dz3_md.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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


    @Test
    void fullCrudFlow() throws Exception {
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

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attributeExists("page"));

        mvc.perform(get("/projects/{id}", p.getProjectId()))
                .andExpect(status().isOk())
                .andExpect(view().name("project-form"))
                .andExpect(model().attribute("project",
                        hasProperty("projectId", equalTo(p.getProjectId()))));

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

        mvc.perform(post("/projects/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Project deleted successfully!"))
                .andExpect(redirectedUrl("/projects"));

        assertThat(projectRepository.existsById(id)).isFalse();
    }
}
