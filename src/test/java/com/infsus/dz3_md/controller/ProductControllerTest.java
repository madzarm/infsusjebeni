package com.infsus.dz3_md.controller;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.service.ProductService;
import com.infsus.dz3_md.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProductController controller;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // ALWAYS stub the dropdown population
        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of()));  // or Page.empty()

        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/jsp/");
        vr.setSuffix(".jsp");

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(vr)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new NoOpValidator())
                .build();
    }

    static class NoOpValidator implements Validator {
        @Override public boolean supports(Class<?> clazz) { return true; }
        @Override public void validate(Object target, Errors errors) { }
    }

    @Test
    void listProducts_noFilters() throws Exception {
        // stub dropdown population
        List<Project> projects = List.of(new Project());
        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(projects));
        // stub page
        Page<Product> page = new PageImpl<>(List.of());
        when(productService.search(null, null, PageRequest.of(0,10)))
                .thenReturn(page);

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attribute("allProjects", projects))
                .andExpect(model().attribute("page", page))
                .andExpect(model().attribute("projectId", nullValue()))
                .andExpect(model().attribute("q", nullValue()));
    }

    @Test
    void listProducts_withFilters() throws Exception {
        UUID pid = UUID.randomUUID();
        List<Project> projects = List.of(new Project());
        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(projects));

        Page<Product> page = new PageImpl<>(List.of());
        when(productService.search(eq(pid), eq("foo"), eq(PageRequest.of(0,10))))
                .thenReturn(page);

        mvc.perform(get("/products")
                        .param("projectId", pid.toString())
                        .param("q", "foo")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attribute("projectId", pid))
                .andExpect(model().attribute("q", "foo"))
                .andExpect(model().attribute("page", page));
    }

    @Test
    void showCreateForm_noProject() throws Exception {
        List<Project> projects = List.of(new Project());
        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(projects));

        mvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attribute("allProjects", projects))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", hasProperty("project", nullValue())));
    }

    @Test
    void showCreateForm_withProject() throws Exception {
        UUID pid = UUID.randomUUID();
        Project proj = new Project();
        proj.setProjectId(pid);
        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(proj)));
        when(projectService.findById(pid)).thenReturn(proj);

        mvc.perform(get("/products/new")
                        .param("projectId", pid.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attribute("allProjects", hasItem(proj)))
                .andExpect(model().attribute("product", hasProperty("project", is(proj))));
    }

    @Test
    void showEditForm_found() throws Exception {
        UUID id = UUID.randomUUID();
        Product prod = new Product();
        when(productService.findById(id)).thenReturn(prod);

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attribute("product", prod));
    }

    @Test
    void showEditForm_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("not found")).when(productService).findById(id);

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "not found"))
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    void saveProduct_validationErrors() throws Exception {
        // no parameters -> BindingResult.hasErrors()==true
        mvc.perform(post("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"));
    }

    @Test
    void saveProduct_success() throws Exception {
        UUID pid = UUID.randomUUID();
        Project proj = new Project();
        proj.setProjectId(pid);

        // stub dropdown
        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(proj)));

        // returned saved product
        Product saved = new Product();
        saved.setProject(proj);
        when(productService.save(ArgumentMatchers.any(Product.class))).thenReturn(saved);

        mvc.perform(post("/products")
                        .param("name", "Widget")
                        .param("project.projectId", pid.toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Product saved successfully!"))
                .andExpect(redirectedUrl("/projects/" + pid));
    }

    @Test
    void saveProduct_serviceThrowsIllegalState() throws Exception {
        UUID pid = UUID.randomUUID();
        Project proj = new Project();
        proj.setProjectId(pid);

        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(proj)));
        when(productService.save(ArgumentMatchers.any(Product.class)))
                .thenThrow(new IllegalStateException("dup"));

        mvc.perform(post("/products")
                        .param("name", "Widget")
                        .param("project.projectId", pid.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name"));
    }

    @Test
    void saveProduct_serviceThrowsGeneric() throws Exception {
        UUID pid = UUID.randomUUID();
        Project proj = new Project();
        proj.setProjectId(pid);

        when(projectService.search(isNull(), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(proj)));
        when(productService.save(ArgumentMatchers.any(Product.class)))
                .thenThrow(new RuntimeException("no proj"));

        mvc.perform(post("/products")
                        .param("name", "Widget")
                        .param("project.projectId", pid.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "project"));
    }

    @Test
    void deleteProduct_success() throws Exception {
        UUID pid = UUID.randomUUID();
        UUID id  = UUID.randomUUID();
        Project proj = new Project(); proj.setProjectId(pid);
        Product prod = new Product(); prod.setProject(proj);

        when(productService.findById(id)).thenReturn(prod);
        doNothing().when(productService).delete(id);

        mvc.perform(post("/products/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("msg", "Product deleted successfully!"))
                .andExpect(redirectedUrl("/projects/" + pid));
    }

    @Test
    void deleteProduct_serviceThrows() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("boom")).when(productService).findById(id);

        mvc.perform(post("/products/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "boom"))
                .andExpect(redirectedUrl("/products"));
    }
}
