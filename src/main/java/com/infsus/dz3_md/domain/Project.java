package com.infsus.dz3_md.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID projectId;

    @NotBlank(message = "Project name is required")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String mission;

    @Column(length = 2000)
    private String vision;

    @NotNull(message = "Average total assets is required")
    @Positive(message = "Average total assets must be greater than 0")
    private Integer averageTotalAssets;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt = LocalDateTime.now();

    private UUID userId;

    @OneToMany(mappedBy = "project",
               fetch = FetchType.EAGER)
    @Valid
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    public List<Product> getProducts() {
        return products;
    }
}
