package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;

    @Autowired        // constructor injection
    public ProjectService(ProjectRepository repo) {
        this.repo = repo;
    }

    public Page<Project> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return repo.findAll(pageable);
        return repo.findAll(ProjectSpecs.nameMissionVisionLike(q), pageable);
    }


    @Transactional(readOnly = true)
    public Project findOne(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
    }


    public Project save(Project p) { return repo.save(p); }

    public void delete(UUID id) {
        Project p = repo.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Project"));
        if (!p.getProducts().isEmpty())      // VALIDATION 3 – custom rule
            throw new IllegalStateException("Cannot delete project with products.");
        repo.delete(p);
    }
}
