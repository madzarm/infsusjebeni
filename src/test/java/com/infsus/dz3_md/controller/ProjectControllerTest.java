package com.infsus.dz3_md.controller;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerTest {

    @Mock
    private ProjectService service;

    @InjectMocks
    private ProjectController controller;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");

        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver();

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
    }

    @Test
    void listProjects_noQuery() throws Exception {
        Page<Project> page = new PageImpl<>(List.of());
        when(service.search(null, PageRequest.of(0,10))).thenReturn(page);

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attribute("page", page))
                .andExpect(model().attribute("q", nullValue()));
    }

    @Test
    void showCreateForm() throws Exception {
        mvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-form"))
                .andExpect(model().attributeExists("project"));
    }

    @Test
    void createProject_validationErrors() throws Exception {
        mvc.perform(post("/projects")
                        .param("name", "Test")
                        .param("status", "ACTIVE")
                        .param("mission", "some mission")
                        .param("vision", "some vision")
                        .param("description", "foo")
                        .param("averageTotalAssets", "-5")
                )
                .andExpect(view().name("project-form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("project"));
    }

    @Test
    void createProject_success() throws Exception {
        mvc.perform(post("/projects")
                        .param("name", "Test")
                        .param("status", "ACTIVE")
                        .param("mission", "some mission")
                        .param("vision", "some vision")
                        .param("description", "foo")
                        .param("averageTotalAssets", "5")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Project created successfully!"))
                .andExpect(redirectedUrl("/projects"));
    }

    @Test
    void showEditForm_found() throws Exception {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        when(service.findById(id)).thenReturn(p);
        mvc.perform(get("/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("project-form"))
                .andExpect(model().attribute("project", p));
    }

    @Test
    void showEditForm_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException()).when(service).findById(id);
        mvc.perform(get("/projects/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Project not found."))
                .andExpect(redirectedUrl("/projects"));
    }

    @Test
    void updateProject_validationErrors() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(post("/projects/{id}", id)
                        .param("name", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("project-form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void updateProject_success() throws Exception {
        UUID id = UUID.randomUUID();
        Project existing = new Project();
        existing.setName("old");
        when(service.findById(id)).thenReturn(existing);

        mvc.perform(post("/projects/{id}", id)
                        .param("name", "new")
                        .param("status", "ACTIVE")
                        .param("mission", "some mission")
                        .param("vision", "some vision")
                        .param("description", "foo")
                        .param("averageTotalAssets", "6")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Project updated successfully!"))
                .andExpect(redirectedUrl("/projects"));

        assertThat(existing.getName()).isEqualTo("new");
    }

    @Test
    void deleteProject_success() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mvc.perform(post("/projects/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Project deleted successfully!"))
                .andExpect(redirectedUrl("/projects"));
    }

    @Test
    void deleteProject_serviceThrows() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("oops")).when(service).delete(id);

        mvc.perform(post("/projects/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "oops"))
                .andExpect(redirectedUrl("/projects"));
    }
}
