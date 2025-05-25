package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    @Mock
    private ProjectRepository repo;

    @InjectMocks
    private ProjectService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void search_withBlankQuery_callsFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(new Project()));
        when(repo.findAll(pageable)).thenReturn(page);

        Page<Project> result = service.search("   ", pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findAll(pageable);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void search_withQuery_callsSpecification() {
        Pageable pageable = PageRequest.of(0, 5);
        String q = "foo";
        Page<Project> page = new PageImpl<>(List.of(new Project()));
        when(repo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Project> result = service.search(q, pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findById_found() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        when(repo.findById(id)).thenReturn(Optional.of(p));

        Project result = service.findById(id);

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

    @Test
    void save_delegatesToRepo() {
        Project p = new Project();
        when(repo.save(p)).thenReturn(p);

        Project result = service.save(p);

        assertThat(result).isSameAs(p);
        verify(repo).save(p);
    }

    @Test
    void delete_existingWithoutProducts_deletes() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        p.setProducts(Collections.emptyList());
        when(repo.findById(id)).thenReturn(Optional.of(p));

        service.delete(id);

        verify(repo).delete(p);
    }

    @Test
    void delete_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void delete_withProducts_throwsIllegalState() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        Product product = new Product();
        p.setProducts(List.of(product));
        when(repo.findById(id)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete project with associated products.");
    }
}
