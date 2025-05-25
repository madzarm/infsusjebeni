package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository repo;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProductService service;

    private Pageable pageable;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        pageable = PageRequest.of(0, 10);
    }

    // --- search() ---

    @Test
    void search_noProject_noQuery_callsFindAll() {
        Page<Product> page = new PageImpl<>(List.of());
        when(repo.findAll(pageable)).thenReturn(page);

        Page<Product> result = service.search(null, null, pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findAll(pageable);
    }

    @Test
    void search_noProject_withQuery_callsFindByName() {
        Page<Product> page = new PageImpl<>(List.of());
        when(repo.findByNameContainingIgnoreCase("foo", pageable))
                .thenReturn(page);

        Page<Product> result = service.search(null, "foo", pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findByNameContainingIgnoreCase("foo", pageable);
    }

    @Test
    void search_withProject_noQuery_callsFindByProject() {
        UUID pid = UUID.randomUUID();
        Page<Product> page = new PageImpl<>(List.of());
        when(repo.findByProject_ProjectId(pid, pageable))
                .thenReturn(page);

        Page<Product> result = service.search(pid, null, pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findByProject_ProjectId(pid, pageable);
    }

    @Test
    void search_withProject_withQuery_callsFindByProjectAndName() {
        UUID pid = UUID.randomUUID();
        Page<Product> page = new PageImpl<>(List.of());
        when(repo.findByProject_ProjectIdAndNameContainingIgnoreCase(pid, "foo", pageable))
                .thenReturn(page);

        Page<Product> result = service.search(pid, "foo", pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findByProject_ProjectIdAndNameContainingIgnoreCase(pid, "foo", pageable);
    }

    // --- findById() ---

    @Test
    void findById_found() {
        UUID id = UUID.randomUUID();
        Product p = new Product();
        when(repo.findById(id)).thenReturn(Optional.of(p));

        Product result = service.findById(id);

        assertThat(result).isSameAs(p);
    }

    @Test
    void findById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // --- save() ---

    @Test
    void save_noProject_throwsIllegalArgument() {
        Product p = new Product();
        p.setProject(null);

        assertThatThrownBy(() -> service.save(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Every product must belong to a project.");
    }

    @Test
    void save_duplicateNewName_throwsIllegalState() {
        UUID pid = UUID.randomUUID();
        Project proj = new Project(); proj.setProjectId(pid);

        Product p = new Product();
        p.setProject(proj);
        p.setName("X");

        when(projectService.findById(pid)).thenReturn(proj);
        when(repo.existsByNameAndProject_ProjectId("X", pid)).thenReturn(true);

        assertThatThrownBy(() -> service.save(p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A product with this name already exists in the project.");
    }

    @Test
    void save_duplicateExistingName_throwsIllegalState() {
        UUID pid = UUID.randomUUID();
        Project proj = new Project(); proj.setProjectId(pid);
        UUID existingId = UUID.randomUUID();

        Product p = new Product();
        p.setProject(proj);
        p.setName("X");
        p.setProductId(existingId);

        when(projectService.findById(pid)).thenReturn(proj);
        when(repo.existsByNameAndProject_ProjectIdAndProductIdNot("X", pid, existingId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.save(p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A product with this name already exists in the project.");
    }

    @Test
    void save_happyPath_savesAndCascades() {
        UUID pid = UUID.randomUUID();
        Project proj = new Project(); proj.setProjectId(pid);
        Product p = new Product();
        p.setProject(proj);
        p.setName("NewProd");

        when(projectService.findById(pid)).thenReturn(proj);
        when(repo.existsByNameAndProject_ProjectId("NewProd", pid)).thenReturn(false);
        when(repo.save(p)).thenReturn(p);

        Product saved = service.save(p);

        assertThat(saved).isSameAs(p);
        verify(repo).save(p);
        verify(projectService).save(proj);
    }

    // --- delete() ---

    @Test
    void delete_notExists_throwsEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void delete_exists_deletes() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repo).deleteById(id);
    }
}

