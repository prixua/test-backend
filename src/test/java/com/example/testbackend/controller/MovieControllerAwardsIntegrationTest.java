package com.example.testbackend.controller;

import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import com.example.testbackend.dto.response.ImportResponse;
import com.example.testbackend.dto.response.ProducerIntervalResponse;
import com.example.testbackend.model.Movie;
import com.example.testbackend.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class MovieControllerAwardsIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        movieRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve analisar intervalos entre prêmios após importação")
    void shouldAnalyzeAwardsIntervalsAfterImport() throws Exception {
        // Given - Importando dados de teste
        String importUuid = importTestCsvFile();

        // When - Fazendo análise de prêmios
        MvcResult result = mockMvc.perform(get("/api/v1/movies/import/{uuidImport}/awards", importUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.min").isArray())
                .andExpect(jsonPath("$.max").isArray())
                .andReturn();

        // Then - Verificando estrutura da resposta
        String responseContent = result.getResponse().getContentAsString();
        SummarizedAwardsResponse awardsResponse = objectMapper.readValue(responseContent, SummarizedAwardsResponse.class);

        assertThat(awardsResponse).isNotNull();
        assertThat(awardsResponse.getMin()).isNotNull();
        assertThat(awardsResponse.getMax()).isNotNull();

        // Verificando se há intervalos mínimos e máximos
        if (!awardsResponse.getMin().isEmpty()) {
            ProducerIntervalResponse minInterval = awardsResponse.getMin().getFirst();
            assertThat(minInterval.getProducer()).isNotNull();
            assertThat(minInterval.getInterval()).isPositive();
            assertThat(minInterval.getPreviousWin()).isPositive();
            assertThat(minInterval.getFollowingWin()).isGreaterThan(minInterval.getPreviousWin());
        }

        if (!awardsResponse.getMax().isEmpty()) {
            ProducerIntervalResponse maxInterval = awardsResponse.getMax().getFirst();
            assertThat(maxInterval.getProducer()).isNotNull();
            assertThat(maxInterval.getInterval()).isPositive();
            assertThat(maxInterval.getPreviousWin()).isPositive();
            assertThat(maxInterval.getFollowingWin()).isGreaterThan(maxInterval.getPreviousWin());
        }
    }

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
    @DisplayName("Deve listar UUIDs de importação após múltiplas importações")
    void shouldListImportUuidsAfterMultipleImports() throws Exception {
        // Given - Múltiplas importações
        String uuid1 = importTestCsvFile();
        String uuid2 = importTestCsvFile();
        String uuid3 = importTestCsvFile();

        // When - Listando UUIDs
        MvcResult result = mockMvc.perform(get("/api/v1/movies/import/uuids"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andReturn();

        // Then - Verificando se todos os UUIDs estão na lista
        String responseContent = result.getResponse().getContentAsString();
        List<String> uuids = objectMapper.readValue(responseContent, List.class);

        assertThat(uuids).containsExactlyInAnyOrder(uuid1, uuid2, uuid3);
    }

    @Test
    @DisplayName("Deve buscar filmes por UUID de importação específico")
    void shouldGetMoviesBySpecificImportUuid() throws Exception {
        // Given - Importação de dados
        String importUuid = importTestCsvFile();

        // When - Buscando filmes por UUID
        MvcResult result = mockMvc.perform(get("/api/v1/movies/import/{importUuid}/list", importUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(32))
                .andReturn();

        // Then - Verificando estrutura dos filmes retornados
        String responseContent = result.getResponse().getContentAsString();
        List<?> movies = objectMapper.readValue(responseContent, List.class);

        assertThat(movies).hasSize(32);

        // Verificando se todos os filmes têm o UUID correto
        List<Movie> savedMovies = movieRepository.findByImportUuid(importUuid);
        assertThat(savedMovies).allMatch(movie -> movie.getImportUuid().equals(importUuid));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para UUID inexistente ao buscar filmes")
    void shouldReturnEmptyListForNonExistentUuidWhenSearchingMovies() throws Exception {
        // Given - UUID que não existe
        String nonExistentUuid = "550e8400-e29b-41d4-a716-446655440000";

        // When & Then
        mockMvc.perform(get("/api/v1/movies/import/{importUuid}/list", nonExistentUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Deve processar corretamente filmes com e sem prêmios")
    void shouldProcessMoviesWithAndWithoutAwards() throws Exception {
        // Given - Importação e análise
        String importUuid = importTestCsvFile();

        // Verificando dados salvos diretamente no banco
        List<Movie> allMovies = movieRepository.findByImportUuid(importUuid);
        List<Movie> winners = allMovies.stream().filter(Movie::getWinner).toList();
        List<Movie> nonWinners = allMovies.stream().filter(movie -> !movie.getWinner()).toList();

        // Then - Verificações
        assertThat(allMovies).hasSize(32);
        assertThat(winners).hasSize(6); // Filmes com "yes" no CSV
        assertThat(nonWinners).hasSize(26); // Filmes sem "yes" no CSV

        // Verificando filmes ganhadores específicos
        assertThat(winners).extracting(Movie::getTitle).containsExactlyInAnyOrder(
                "Can't Stop the Music",
                "Xanadu",
                "Mommie Dearest",
                "Inchon",
                "The Lonely Lady",
                "Bolero"
        );

    }

    @Test
    @DisplayName("Deve criar arquivo CSV com dados específicos para teste de intervalos")
    void shouldCreateSpecificCsvForIntervalTesting() throws Exception {
        // Given & When - Importando arquivo CSV específico para teste de intervalos
        MvcResult importResult = importCsvFileAndGetResult("interval-test.csv");
        ImportResponse importResponse = objectMapper.readValue(
                importResult.getResponse().getContentAsString(),
                ImportResponse.class
        );

        // When - Analisando intervalos
        MvcResult analysisResult = mockMvc.perform(get("/api/v1/movies/import/{uuidImport}/awards", importResponse.getUuidImported()))
                .andExpect(status().isOk())
                .andReturn();

        SummarizedAwardsResponse awardsResponse = objectMapper.readValue(
                analysisResult.getResponse().getContentAsString(),
                SummarizedAwardsResponse.class
        );

        // Then - Verificando intervalos específicos
        assertThat(awardsResponse.getMin()).hasSize(1);
        assertThat(awardsResponse.getMax()).hasSize(1);

        ProducerIntervalResponse minInterval = awardsResponse.getMin().getFirst();
        assertThat(minInterval.getProducer()).isEqualTo("Albert S. Ruddy");
        assertThat(minInterval.getInterval()).isEqualTo(1); // 1991 - 1990
        assertThat(minInterval.getPreviousWin()).isEqualTo(1990);
        assertThat(minInterval.getFollowingWin()).isEqualTo(1991);

        ProducerIntervalResponse maxInterval = awardsResponse.getMax().getFirst();
        assertThat(maxInterval.getProducer()).isEqualTo("Matthew Vaughn");
        assertThat(maxInterval.getInterval()).isEqualTo(13); // 2013 - 2000
        assertThat(maxInterval.getPreviousWin()).isEqualTo(2000);
        assertThat(maxInterval.getFollowingWin()).isEqualTo(2013);
    }

    @Test
    @DisplayName("Deve validar intervalos específicos com arquivo movielist.csv - arquivo fornecido")
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

    @Test
    @DisplayName("Deve validar intervalos específicos com arquivo movielist-coincident.csv")
    void shouldValidateSpecificIntervalsWithMovielistCoincidentCsv() throws Exception {
        // Given & When - Importando arquivo movielist-coincident.csv
        MvcResult importResult = importCsvFileAndGetResult("movielist-coincident.csv");
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
        assertThat(awardsResponse.getMin()).hasSize(2);
        assertThat(awardsResponse.getMax()).hasSize(1);

        // Verificando intervalos mínimos (Bo Derek e Joel Silver)
        List<ProducerIntervalResponse> minIntervals = awardsResponse.getMin();

        // Verificando se Bo Derek está na lista
        ProducerIntervalResponse boDerekInterval = minIntervals.stream()
                .filter(interval -> "Bo Derek".equals(interval.getProducer()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Bo Derek não encontrado nos intervalos mínimos"));

        assertThat(boDerekInterval.getInterval()).isEqualTo(1);
        assertThat(boDerekInterval.getPreviousWin()).isEqualTo(1990);
        assertThat(boDerekInterval.getFollowingWin()).isEqualTo(1991);

        // Verificando se Joel Silver está na lista
        ProducerIntervalResponse joelSilverInterval = minIntervals.stream()
                .filter(interval -> "Joel Silver".equals(interval.getProducer()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Joel Silver não encontrado nos intervalos mínimos"));

        assertThat(joelSilverInterval.getInterval()).isEqualTo(1);
        assertThat(joelSilverInterval.getPreviousWin()).isEqualTo(1990);
        assertThat(joelSilverInterval.getFollowingWin()).isEqualTo(1991);

        // Verificando intervalo máximo (Matthew Vaughn)
        ProducerIntervalResponse maxInterval = awardsResponse.getMax().get(0);
        assertThat(maxInterval.getProducer()).isEqualTo("Matthew Vaughn");
        assertThat(maxInterval.getInterval()).isEqualTo(13);
        assertThat(maxInterval.getPreviousWin()).isEqualTo(2002);
        assertThat(maxInterval.getFollowingWin()).isEqualTo(2015);
    }

    private String importTestCsvFile() throws Exception {
        InputStream csvInputStream = getClass().getClassLoader()
                .getResourceAsStream("mocks/test-movies.csv");

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "mocks/test-movies.csv",
                "text/csv",
                csvInputStream
        );

        MvcResult result = mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(csvFile))
                .andExpect(status().isCreated())
                .andReturn();

        ImportResponse importResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ImportResponse.class
        );

        return importResponse.getUuidImported();
    }

    /**
     * Método helper para importar arquivo CSV e retornar o MvcResult completo
     */
    private MvcResult importCsvFileAndGetResult(String fileName) throws Exception {
        InputStream csvInputStream = getClass().getClassLoader()
                .getResourceAsStream("mocks/" + fileName);
        assertThat(csvInputStream).isNotNull();

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                fileName,
                "text/csv",
                csvInputStream
        );

        return mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(csvFile))
                .andExpect(status().isCreated())
                .andReturn();
    }

    /**
     * Método helper para importar arquivo CSV e retornar o objeto ImportResponse
     */
    private ImportResponse importCsvFileAndGetResponse(String fileName) throws Exception {
        MvcResult result = importCsvFileAndGetResult(fileName);
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ImportResponse.class
        );
    }

    /**
     * Método helper para importar arquivo CSV e retornar apenas o UUID
     */
    private String importCsvFileAndGetUuid(String fileName) throws Exception {
        return importCsvFileAndGetResponse(fileName).getUuidImported();
    }
}
