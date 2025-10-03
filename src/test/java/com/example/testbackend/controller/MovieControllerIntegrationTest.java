package com.example.testbackend.controller;

import com.example.testbackend.dto.response.ImportResponse;
import com.example.testbackend.dto.response.ProducerIntervalResponse;
import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import com.example.testbackend.model.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MovieControllerIntegrationTest extends AbstractControllerIntegrationTest {

    // ========== TESTES DE IMPORTAÇÃO ==========

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void shouldRejectEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Arquivo não pode estar vazio"));

        // Verificando que nenhum filme foi salvo
        List<Movie> allMovies = movieRepository.findAll();
        assertThat(allMovies).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar arquivo que não é CSV")
    void shouldRejectNonCsvFile() throws Exception {
        // Given
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "conteúdo qualquer".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(txtFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Arquivo deve ser do tipo CSV"));

        // Verificando que nenhum filme foi salvo
        List<Movie> allMovies = movieRepository.findAll();
        assertThat(allMovies).isEmpty();
    }

    // ========== TESTES DE ANÁLISE DE PRÊMIOS ==========

    @Test
    @DisplayName("Deve retornar erro 404 para UUID inexistente na análise de prêmios")
    void shouldReturn404ForNonExistentUuidInAwardsAnalysis() throws Exception {
        // Given - UUID que não existe
        String nonExistentUuid = "550e8400-e29b-41d4-a716-446655440000";

        // When & Then
        mockMvc.perform(get("/api/v1/movies/import/{uuidImport}/awards", nonExistentUuid))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Nenhum filme encontrado para o UUID de importação: " + nonExistentUuid));
    }

    @Test
    @DisplayName("Deve validar intervalos específicos com arquivo movielist.csv")
    void shouldValidateSpecificIntervalsWithMovielistCsv() throws Exception {
        // Given & When - Importando arquivo movielist.csv
        MvcResult importResult = importCsvFileAndGetResult("movielist.csv");
        ImportResponse importResponse = objectMapper.readValue(
                importResult.getResponse().getContentAsString(),
                ImportResponse.class
        );

        // When - Analisando intervalos de prêmios
        MvcResult analysisResult = mockMvc.perform(get("/api/v1/movies/import/{uuidImport}/awards", importResponse.getUuidImported()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        SummarizedAwardsResponse awardsResponse = objectMapper.readValue(
                analysisResult.getResponse().getContentAsString(),
                SummarizedAwardsResponse.class
        );

        // Then - Verificando estrutura da resposta
        assertThat(awardsResponse).isNotNull();
        assertThat(awardsResponse.getMin()).hasSize(1);
        assertThat(awardsResponse.getMax()).hasSize(1);

        // Verificando intervalo mínimo (Joel Silver)
        ProducerIntervalResponse minInterval = awardsResponse.getMin().getFirst();
        assertThat(minInterval.getProducer()).isEqualTo("Joel Silver");
        assertThat(minInterval.getInterval()).isEqualTo(1);
        assertThat(minInterval.getPreviousWin()).isEqualTo(1990);
        assertThat(minInterval.getFollowingWin()).isEqualTo(1991);

        // Verificando intervalo máximo (Matthew Vaughn)
        ProducerIntervalResponse maxInterval = awardsResponse.getMax().getFirst();
        assertThat(maxInterval.getProducer()).isEqualTo("Matthew Vaughn");
        assertThat(maxInterval.getInterval()).isEqualTo(13);
        assertThat(maxInterval.getPreviousWin()).isEqualTo(2002);
        assertThat(maxInterval.getFollowingWin()).isEqualTo(2015);
    }
}
