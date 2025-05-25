package com.infsus.dz3_md.repository;

import com.infsus.dz3_md.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pg);
    Page<Product> findByProject_ProjectId(UUID projectId, Pageable pg);
    Page<Product> findByProject_ProjectIdAndNameContainingIgnoreCase(
            UUID projectId, String q, Pageable pg);
    boolean existsByNameAndProject_ProjectId(String name, UUID projectId);
    boolean existsByNameAndProject_ProjectIdAndProductIdNot(String name, UUID projectId, UUID productId);
}