package com.example.testbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Resposta da importação de arquivo CSV")
public class ImportResponse {

    @Schema(
            description = "UUID único gerado para identificar esta importação específica",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
    )
    private String uuidImported;
}
