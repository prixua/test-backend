package com.example.testbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ano é obrigatório")
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String studios;

    @Column(length = 500)
    private String producers;

    @Builder.Default
    @Column(nullable = false)
    private Boolean winner = false;

    @NotBlank(message = "UUID de importação é obrigatório")
    @Column(nullable = false, length = 64)
    private String importUuid;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
