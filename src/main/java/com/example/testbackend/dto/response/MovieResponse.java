package com.example.testbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Dados completos de um filme")
public class MovieResponse {

    @Schema(description = "ID único do filme no banco de dados", example = "1")
    private Long id;

    @Schema(description = "Ano de lançamento do filme", example = "1981")
    private Integer year;

    @Schema(description = "Título do filme", example = "Tarzan, the Ape Man")
    private String title;

    @Schema(description = "Estúdios responsáveis pela produção", example = "MGM, United Artists")
    private String studios;

    @Schema(description = "Produtores do filme", example = "John Derek")
    private String producers;

    @Schema(description = "Indica se o filme ganhou o prêmio (Razzie)", example = "true")
    private Boolean winner;

    @Schema(
            description = "UUID da importação que incluiu este filme",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String importUuid;
    private LocalDateTime createdAt;
}
