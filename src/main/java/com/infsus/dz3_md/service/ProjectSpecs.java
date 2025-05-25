package com.infsus.dz3_md.service;

import com.infsus.dz3_md.domain.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class ProjectSpecs {

    private ProjectSpecs() { }   // utility class

    /** Case-insensitive “LIKE %q%” against name, mission or vision. */
    public static Specification<Project> nameMissionVisionLike(String q) {
        return (root, query, cb) -> {
            String like = "%" + q.toLowerCase() + "%";
            Predicate pName    = cb.like(cb.lower(root.get("name")),     like);
            Predicate pMission = cb.like(cb.lower(root.get("mission")),  like);
            Predicate pVision  = cb.like(cb.lower(root.get("vision")),   like);
            return cb.or(pName, pMission, pVision);
        };
    }
}
