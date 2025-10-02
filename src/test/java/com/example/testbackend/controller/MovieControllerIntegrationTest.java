package com.example.testbackend.controller;

import com.example.testbackend.dto.response.ImportResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class MovieControllerIntegrationTest {

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
    @DisplayName("Deve importar arquivo CSV e salvar filmes na base de dados")
    void shouldImportCsvFileAndSaveMoviesToDatabase() throws Exception {
        // Given - Carregando arquivo CSV de teste
        InputStream csvInputStream = getClass().getClassLoader()
                .getResourceAsStream("mocks/test-movies.csv");
        assertThat(csvInputStream).isNotNull();

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "mocks/test-movies.csv",
                "text/csv",
                csvInputStream
        );

        // When - Fazendo a requisição de importação
        MvcResult result = mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(csvFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuidImported").exists())
                .andReturn();

        // Then - Verificando se o UUID foi retornado
        String responseContent = result.getResponse().getContentAsString();
        ImportResponse importResponse = objectMapper.readValue(responseContent, ImportResponse.class);

        assertThat(importResponse.getUuidImported()).isNotNull();

        // Verificando se os dados foram salvos na base
        List<Movie> savedMovies = movieRepository.findByImportUuid(importResponse.getUuidImported());

        // Verificações gerais
        assertThat(savedMovies).hasSize(32); // Total de filmes no CSV (excluindo header)
        assertThat(savedMovies).allMatch(movie -> movie.getImportUuid().equals(importResponse.getUuidImported()));

        // Verificando filmes ganhadores
        List<Movie> winners = savedMovies.stream()
                .filter(Movie::getWinner)
                .toList();
        assertThat(winners).hasSize(6); // Filmes com "yes" na coluna winner

        // Verificando dados específicos de alguns filmes
        verifySpecificMovieData(savedMovies);
    }

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

    @Test
    @DisplayName("Deve importar múltiplos arquivos com UUIDs diferentes")
    void shouldImportMultipleFilesWithDifferentUuids() throws Exception {
        // Given - Primeiro arquivo
        InputStream csvInputStream1 = getClass().getClassLoader()
                .getResourceAsStream("mocks/test-movies.csv");
        MockMultipartFile csvFile1 = new MockMultipartFile(
                "file",
                "movies1.csv",
                "text/csv",
                csvInputStream1
        );

        // When - Primeira importação
        MvcResult result1 = mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(csvFile1))
                .andExpect(status().isCreated())
                .andReturn();

        ImportResponse response1 = objectMapper.readValue(
                result1.getResponse().getContentAsString(),
                ImportResponse.class
        );

        // Given - Segundo arquivo (mesmo conteúdo, mas deve ter UUID diferente)
        InputStream csvInputStream2 = getClass().getClassLoader()
                .getResourceAsStream("mocks/test-movies.csv");
        MockMultipartFile csvFile2 = new MockMultipartFile(
                "file",
                "movies2.csv",
                "text/csv",
                csvInputStream2
        );

        // When - Segunda importação
        MvcResult result2 = mockMvc.perform(multipart("/api/v1/movies/import")
                        .file(csvFile2))
                .andExpect(status().isCreated())
                .andReturn();

        ImportResponse response2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(),
                ImportResponse.class
        );

        // Then - Verificando que os UUIDs são diferentes
        assertThat(response1.getUuidImported()).isNotEqualTo(response2.getUuidImported());

        // Verificando que ambas as importações foram salvas
        List<Movie> movies1 = movieRepository.findByImportUuid(response1.getUuidImported());
        List<Movie> movies2 = movieRepository.findByImportUuid(response2.getUuidImported());

        assertThat(movies1).hasSize(32);
        assertThat(movies2).hasSize(32);
        assertThat(movieRepository.count()).isEqualTo(64); // Total de filmes
    }

    private void verifySpecificMovieData(List<Movie> savedMovies) {
        // Verificando filme "Can't Stop the Music" (1980)
        Optional<Movie> canStopMusic = savedMovies.stream()
                .filter(movie -> "Can't Stop the Music".equals(movie.getTitle()))
                .findFirst();

        assertThat(canStopMusic).isPresent();
        Movie canStopMusicMovie = canStopMusic.get();
        assertThat(canStopMusicMovie.getYear()).isEqualTo(1980);
        assertThat(canStopMusicMovie.getStudios()).isEqualTo("Associated Film Distribution");
        assertThat(canStopMusicMovie.getProducers()).isEqualTo("Allan Carr");
        assertThat(canStopMusicMovie.getWinner()).isTrue();

        // Verificando filme "Inchon" (1982)
        Optional<Movie> inchon = savedMovies.stream()
                .filter(movie -> "Inchon".equals(movie.getTitle()))
                .findFirst();

        assertThat(inchon).isPresent();
        Movie inchonMovie = inchon.get();
        assertThat(inchonMovie.getYear()).isEqualTo(1982);
        assertThat(inchonMovie.getStudios()).isEqualTo("MGM");
        assertThat(inchonMovie.getProducers()).isEqualTo("Mitsuharu Ishii");
        assertThat(inchonMovie.getWinner()).isTrue();

        // Verificando filme sem prêmio "Annie" (1982)
        Optional<Movie> annie = savedMovies.stream()
                .filter(movie -> "Annie".equals(movie.getTitle()))
                .findFirst();

        assertThat(annie).isPresent();
        Movie annieMovie = annie.get();
        assertThat(annieMovie.getYear()).isEqualTo(1982);
        assertThat(annieMovie.getStudios()).isEqualTo("Columbia Pictures");
        assertThat(annieMovie.getProducers()).isEqualTo("Ray Stark");
        assertThat(annieMovie.getWinner()).isFalse();

        // Verificando filme com múltiplos estúdios "Hercules" (1983)
        Optional<Movie> hercules = savedMovies.stream()
                .filter(movie -> "Hercules".equals(movie.getTitle()))
                .findFirst();

        assertThat(hercules).isPresent();
        Movie herculesMovie = hercules.get();
        assertThat(herculesMovie.getYear()).isEqualTo(1983);
        assertThat(herculesMovie.getStudios()).isEqualTo("MGM, United Artists, Cannon Films");
        assertThat(herculesMovie.getProducers()).isEqualTo("Yoram Globus and Menahem Golan");
        assertThat(herculesMovie.getWinner()).isFalse();

        // Verificando que todos os filmes têm timestamp de criação
        assertThat(savedMovies).allMatch(m -> m.getCreatedAt() != null);

        // Verificando anos distintos
        List<Integer> distinctYears = savedMovies.stream()
                .map(Movie::getYear)
                .distinct()
                .sorted()
                .toList();
        assertThat(distinctYears).containsExactly(1980, 1981, 1982, 1983, 1984);
    }
}
