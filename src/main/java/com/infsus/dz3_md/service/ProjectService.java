package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.repository.ProductRepository;
import com.infsus.dz3_md.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Project> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return projectRepository.findAll(pageable);
        }
        return projectRepository.findAll(ProjectSpecs.nameMissionVisionLike(q), pageable);
    }

    @Transactional(readOnly = true)
    public Project findById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
    }

    @Transactional
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public void delete(UUID id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
        var products = project.getProducts();
        productRepository.deleteAll(products);
        projectRepository.delete(project);
    }
}
