package com.infsus.dz3_md.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product",
       uniqueConstraints = @UniqueConstraint(columnNames = {"name", "project_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Product name is required")
    private String name;

    @Column(length = 2000)
    private String description;

    @PositiveOrZero(message = "Price must be >= 0")
    private BigDecimal price;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
}
