package com.example.testbackend.config;

import com.example.testbackend.dto.response.ProducerIntervalResponse;
import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import com.example.testbackend.model.Movie;
import com.example.testbackend.repository.MovieRepository;
import com.example.testbackend.service.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class DataInitializerTest {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieService movieService;

    @Test
    @DisplayName("Deve carregar dados iniciais automaticamente na inicialização e validar o response da análise")
    void shouldLoadInitialDataAutomatically() {
        // Given & When - A aplicação já foi inicializada pelo Spring Boot

        // Then - Verificando se os dados foram carregados
        List<Movie> allMovies = movieRepository.findAll();

        // Verificando se há pelo menos alguns filmes carregados
        assertThat(allMovies).isNotEmpty();

        // Verificando se todos os filmes têm um UUID de importação válido
        assertThat(allMovies).allMatch(movie -> movie.getImportUuid() != null);

        // Verificando se há pelo menos um UUID de importação válido
        List<String> distinctUuids = allMovies.stream()
                .map(Movie::getImportUuid)
                .distinct()
                .toList();
        assertThat(distinctUuids).isNotEmpty();

        // Verificando se há filmes ganhadores e não-ganhadores
        List<Movie> winners = allMovies.stream().filter(Movie::getWinner).toList();
        List<Movie> nonWinners = allMovies.stream().filter(movie -> !movie.getWinner()).toList();

        assertThat(winners).isNotEmpty();
        assertThat(nonWinners).isNotEmpty();

        // Verificando estrutura básica dos dados
        assertThat(allMovies).allMatch(movie ->
            movie.getYear() != null &&
            movie.getTitle() != null &&
            !movie.getTitle().trim().isEmpty()
        );

        // Faz assert pela quantidade de linhas importadas
        assertThat(allMovies.size()).isEqualTo(206);

        // Usa o  UUID do carregamento e chama o method summarizedAwards e verifica o retorno da análise
        String importUuid = distinctUuids.getFirst();
        SummarizedAwardsResponse awardsResponse = movieService.getSummarizedAwards(importUuid);

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
