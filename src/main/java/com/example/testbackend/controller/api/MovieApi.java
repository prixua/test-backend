package com.example.testbackend.controller.api;

import com.example.testbackend.dto.response.ImportResponse;
import com.example.testbackend.dto.response.MovieResponse;
import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Movies", description = "API para gerenciamento de filmes e análise de prêmios")
public interface MovieApi {

    @Operation(
            summary = "Importar arquivo CSV de filmes",
            description = "Importa um arquivo CSV contendo dados de filmes no formato: year;title;studios;producers;winner. " +
                         "Retorna um UUID único para identificar essa importação específica."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Arquivo importado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ImportResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação - arquivo vazio ou formato inválido"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor durante o processamento do arquivo"
            )
    })
    ResponseEntity<ImportResponse> importCsv(
            @Parameter(
                    description = "Arquivo CSV contendo dados dos filmes",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file
    );

    @Operation(
            summary = "Análise de intervalos entre prêmios",
            description = "Analisa os produtores com maior e menor intervalo entre dois prêmios consecutivos " +
                    "baseado nos dados de uma importação específica. Retorna os produtores que ganharam " +
                    "prêmios com o menor intervalo (mais rápido) e maior intervalo (maior lacuna) entre vitórias."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Análise de intervalos retornada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SummarizedAwardsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "UUID de importação não encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "UUID inválido fornecido"
            )
    })
    ResponseEntity<SummarizedAwardsResponse> getSummarizedAwards(
            @Parameter(
                    description = "UUID da importação para análise dos prêmios",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String uuidImport
    );

    @Operation(
            summary = "Listar todos os UUIDs de importação",
            description = "Retorna uma lista com todos os UUIDs de importações realizadas, ordenados pela data de criação (mais recentes primeiro)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de UUIDs retornada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String[].class)
                    )
            )
    })
    ResponseEntity<List<String>> getImportUuids();

    @Operation(
            summary = "Buscar filmes por UUID de importação",
            description = "Retorna todos os filmes que foram importados em uma importação específica, identificada pelo UUID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de filmes retornada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovieResponse[].class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "UUID de importação não encontrado"
            )
    })
    ResponseEntity<List<MovieResponse>> getMoviesByImportUuid(
            @Parameter(
                    description = "UUID da importação para buscar os filmes",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String uuidImport
    );

}
