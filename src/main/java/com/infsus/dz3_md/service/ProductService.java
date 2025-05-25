package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProjectService projectService;

    /**
     * Search products globally or within a project and/or by name
     */
    @Transactional(readOnly = true)
    public Page<Product> search(UUID projectId, String q, Pageable pageable) {
        boolean hasQ = q != null && !q.isBlank();
        if (projectId != null) {
            if (hasQ) {
                return productRepository
                        .findByProject_ProjectIdAndNameContainingIgnoreCase(projectId, q, pageable);
            }
            return productRepository.findByProject_ProjectId(projectId, pageable);
        }
        if (hasQ) {
            return productRepository.findByNameContainingIgnoreCase(q, pageable);
        }
        return productRepository.findAll(pageable);
    }

    /**
     * Retrieve a single product by ID
     */
    @Transactional(readOnly = true)
    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    /**
     * Save or update a product, enforcing project and name-uniqueness
     */
    @Transactional
    public Product save(Product product) {
        UUID projectId = product.getProject() != null
                ? product.getProject().getProjectId() : null;
        if (projectId == null) {
            throw new IllegalArgumentException("Every product must belong to a project.");
        }
        Project project = projectService.findById(projectId);
        product.setProject(project);

        boolean exists;
        if (product.getProductId() == null) {
            exists = productRepository.existsByNameAndProject_ProjectId(
                    product.getName(), projectId);
        } else {
            exists = productRepository.existsByNameAndProject_ProjectIdAndProductIdNot(
                    product.getName(), projectId, product.getProductId());
        }
        if (exists) {
            throw new IllegalStateException(
                    "A product with this name already exists in the project.");
        }

        Product saved = productRepository.save(product);
        projectService.save(project); // cascade update
        return saved;
    }

    /**
     * Delete a product by ID
     */
    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }
}
