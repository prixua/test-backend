package com.example.testbackend.service;

import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import com.example.testbackend.dto.response.ImportResponse;
import com.example.testbackend.dto.response.MovieResponse;
import com.example.testbackend.dto.response.ProducerIntervalResponse;
import com.example.testbackend.exception.ResourceNotFoundException;
import com.example.testbackend.mapper.MovieMapper;
import com.example.testbackend.model.Movie;
import com.example.testbackend.repository.MovieRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Transactional(readOnly = true)
    public List<String> getImportUuids() {
        log.debug("Obtendo todos os UUIDs de importação");
        return movieRepository.findDistinctImportUuids();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByImportUuid(String importUuid) {
        log.debug("Buscando filmes por UUID de importação: {}", importUuid);

        List<Movie> movies = movieRepository.findByImportUuid(importUuid);
        return movies.stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    public ImportResponse importCsvFile(MultipartFile file) {
        log.info("Iniciando importação de CSV para o arquivo: {}", file.getOriginalFilename());

        String importId = generateImportId();
        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;

        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            List<String[]> records = csvReader.readAll();

            // Remove header if exists
            if (!records.isEmpty() && isHeaderRow(records.getFirst())) {
                records.removeFirst();
                log.debug("Cabeçalho detectado e removido");
            }

            totalRecords = records.size();
            List<Movie> moviesToSave = new ArrayList<>();

            for (String[] record : records) {
                try {
                    Movie movie = parseRecordToMovie(record, importId);
                    moviesToSave.add(movie);
                    successfulRecords++;
                } catch (Exception e) {
                    log.warn("Falha ao processar registro: {} - Erro: {}", String.join(";", record), e.getMessage());
                    failedRecords++;
                }
            }

            if (!moviesToSave.isEmpty()) {
                movieRepository.saveAll(moviesToSave);
                log.info("Salvos com sucesso {} filmes com ID de importação: {}", moviesToSave.size(), importId);
            }

        } catch (IOException | CsvException e) {
            log.error("Erro ao ler arquivo CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar arquivo CSV: " + e.getMessage());
        }

        return ImportResponse.builder()
                .uuidImported(importId)
                .build();
    }

    private boolean isHeaderRow(String[] record) {
        return record.length > 0 && "year".equalsIgnoreCase(record[0].trim());
    }

    private Movie parseRecordToMovie(String[] record, String importId) {
        if (record.length < 5) {
            throw new IllegalArgumentException("Registro deve ter pelo menos 5 colunas: year;title;studios;producers;winner");
        }

        try {
            Integer year = Integer.parseInt(record[0].trim());
            String title = record[1].trim();
            String studios = record[2].trim();
            String producers = record[3].trim();
            Boolean winner = "yes".equalsIgnoreCase(record[4].trim());

            return Movie.builder()
                    .year(year)
                    .title(title)
                    .studios(studios.isEmpty() ? null : studios)
                    .producers(producers.isEmpty() ? null : producers)
                    .winner(winner)
                    .importUuid(importId)
                    .build();

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ano deve ser um número válido: " + record[0]);
        }
    }

    public SummarizedAwardsResponse getSummarizedAwards(String importUuid) {
        log.debug("Obtendo análise de prêmios para UUID de importação: {}", importUuid);

        List<Movie> movies = movieRepository.findByImportUuid(importUuid);

        if (movies.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum filme encontrado para o UUID de importação: " + importUuid);
        }

        // Filtrar apenas filmes ganhadores
        List<Movie> winners = movies.stream()
                .filter(Movie::getWinner)
                .toList();

        if (winners.isEmpty()) {
            return SummarizedAwardsResponse.builder()
                    .min(List.of())
                    .max(List.of())
                    .build();
        }

        // Agrupar por produtor e calcular intervalos
        Map<String, List<Integer>> producerYears = new HashMap<>();

        for (Movie movie : winners) {
            if (movie.getProducers() != null && !movie.getProducers().trim().isEmpty()) {
                // Dividir produtores por vírgula, "and" ou ";"
                String[] producers = movie.getProducers().split("[,;]|\\sand\\s");

                for (String producer : producers) {
                    String cleanProducer = producer.trim();
                    if (!cleanProducer.isEmpty()) {
                        producerYears.computeIfAbsent(cleanProducer, k -> new ArrayList<>())
                                .add(movie.getYear());
                    }
                }
            }
        }

        // Calcular TODOS os intervalos para cada produtor
        List<ProducerIntervalResponse> allIntervals = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : producerYears.entrySet()) {
            String producer = entry.getKey();
            List<Integer> years = entry.getValue();

            if (years.size() >= 2) {
                // Remove duplicatas e ordena
                Set<Integer> uniqueYears = new TreeSet<>(years);
                List<Integer> sortedYears = new ArrayList<>(uniqueYears);

                // Calcula todos os intervalos consecutivos para esse produtor
                for (int i = 1; i < sortedYears.size(); i++) {
                    int previousWin = sortedYears.get(i - 1);
                    int followingWin = sortedYears.get(i);
                    int interval = followingWin - previousWin;

                    allIntervals.add(ProducerIntervalResponse.builder()
                            .producer(producer)
                            .interval(interval)
                            .previousWin(previousWin)
                            .followingWin(followingWin)
                            .build());
                }
            }
        }

        if (allIntervals.isEmpty()) {
            return SummarizedAwardsResponse.builder()
                    .min(List.of())
                    .max(List.of())
                    .build();
        }

        // Encontrar menor e maior intervalo
        int minInterval = allIntervals.stream()
                .mapToInt(ProducerIntervalResponse::getInterval)
                .min()
                .orElse(0);

        int maxInterval = allIntervals.stream()
                .mapToInt(ProducerIntervalResponse::getInterval)
                .max()
                .orElse(0);

        // Retornar TODOS os intervalos que são mínimos ou máximos
        List<ProducerIntervalResponse> minIntervals = allIntervals.stream()
                .filter(interval -> interval.getInterval().equals(minInterval))
                .toList();

        List<ProducerIntervalResponse> maxIntervals = allIntervals.stream()
                .filter(interval -> interval.getInterval().equals(maxInterval))
                .toList();

        log.debug("Encontrados {} intervalos mínimos com valor {} e {} intervalos máximos com valor {}",
                 minIntervals.size(), minInterval, maxIntervals.size(), maxInterval);

        return SummarizedAwardsResponse.builder()
                .min(minIntervals)
                .max(maxIntervals)
                .build();
    }

    private String generateImportId() {
        return UUID.randomUUID().toString();
    }
}
