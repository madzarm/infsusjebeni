package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final ProjectService projectService;  // to resolve the FK

    /**
     * Search all products, or by project, and/or by name q.
     */
    @Transactional(readOnly = true)
    public Page<Product> search(UUID projectId, String q, Pageable pg) {
        boolean hasQ = q != null && !q.isBlank();
        if (projectId != null) {
            if (hasQ) {
                return repo.findByProject_ProjectIdAndNameContainingIgnoreCase(projectId, q, pg);
            }
            return repo.findByProject_ProjectId(projectId, pg);
        }
        if (hasQ) {
            return repo.findByNameContainingIgnoreCase(q, pg);
        }
        return repo.findAll(pg);
    }

    @Transactional(readOnly = true)
    public Product findOne(UUID id) {
        return repo.findById(id)
                   .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product save(Product p) {
        UUID pid = p.getProject() != null ? p.getProject().getProjectId() : null;
        if (pid == null) {
            throw new IllegalArgumentException("Every product must belong to a project.");
        }
        // re-attach managed project
        Project pr = projectService.findOne(pid);
        p.setProject(pr);

        // unique-name check
        if (p.getProductId() == null) {
            // creating
            if (repo.existsByNameAndProject_ProjectId(p.getName(), pid)) {
                throw new IllegalStateException("A product with this name already exists in the project.");
            }
        } else {
            // updating
            if (repo.existsByNameAndProject_ProjectIdAndProductIdNot(
                    p.getName(), pid, p.getProductId())) {
                throw new IllegalStateException("A product with this name already exists in the project.");
            }
        }

        p = repo.save(p);
        projectService.save(pr);  // update the project
        return p;
    }

    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
