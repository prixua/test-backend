package com.example.testbackend.service;

import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import com.example.testbackend.dto.response.ImportResponse;
import com.example.testbackend.dto.response.ProducerIntervalResponse;
import com.example.testbackend.exception.ResourceNotFoundException;
import com.example.testbackend.mapper.MovieAwardsMapper;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovieService {

    private static final String YES_SENTENCE = "yes";
    private static final String YEAR_SENTENCE = "year";
    private static final String AT_LEAST_5_COLUMNS_MSG = "Registro deve ter pelo menos 5 colunas: year;title;studios;producers;winner";
    private final MovieRepository movieRepository;
    private final MovieAwardsMapper movieAwardsMapper;

    public ImportResponse importCsvFile(MultipartFile file) {
        log.info("Iniciando importação de CSV para o arquivo: {}", file.getOriginalFilename());

        String importId = generateImportId();

        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            List<String[]> records = csvReader.readAll();

            // Remove header if exists
            if (!records.isEmpty() && isHeaderRow(records.getFirst())) {
                records.removeFirst();
                log.debug("Cabeçalho detectado e removido");
            }

            List<Movie> moviesToSave = records.stream()
                    .map(record -> parseRecordToMovie(record, importId))
                    .toList();

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
        return record.length > 0 && YEAR_SENTENCE.equalsIgnoreCase(record[0].trim());
    }

    private Movie parseRecordToMovie(String[] record, String importId) {
        if (record.length < 5) {
            throw new IllegalArgumentException(AT_LEAST_5_COLUMNS_MSG);
        }

        try {
            Integer year = Integer.parseInt(record[0].trim());
            String title = record[1].trim();
            String studios = record[2].trim();
            String producers = record[3].trim();
            Boolean winner = YES_SENTENCE.equalsIgnoreCase(record[4].trim());

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

        // Agrupando produtores e anos usando Streams: Key = produtor, Value = lista de anos
        Map<String, List<Integer>> producerYears = movies.stream()
                .filter(Movie::getWinner)
                .filter(movie -> movie.getProducers() != null && !movie.getProducers().trim().isEmpty())
                .flatMap(movie -> Arrays.stream(movie.getProducers().split("[,;]|\\sand\\s"))
                        .map(String::trim)
                        .filter(producer -> !producer.isEmpty())
                        .map(producer -> Map.entry(producer, movie.getYear())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        if (producerYears.isEmpty()) {
            return movieAwardsMapper.createEmptyResponse();
        }

        List<ProducerIntervalResponse> allIntervals = calculateAllIntervals(producerYears);

        if (allIntervals.isEmpty()) {
            return movieAwardsMapper.createEmptyResponse();
        }

        return movieAwardsMapper.buildSummarizedResponse(allIntervals);
    }

    private List<ProducerIntervalResponse> calculateAllIntervals(Map<String, List<Integer>> producerYears) {
        return producerYears.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 2)
                .flatMap(entry -> calculateIntervalsForProducer(entry.getKey(), entry.getValue()).stream())
                .toList();
    }

    private List<ProducerIntervalResponse> calculateIntervalsForProducer(String producer, List<Integer> years) {
        List<Integer> sortedYears = new ArrayList<>(new TreeSet<>(years));

        return IntStream.range(1, sortedYears.size())
                .mapToObj(i -> {
                    int previousWin = sortedYears.get(i - 1);
                    int followingWin = sortedYears.get(i);
                    int interval = followingWin - previousWin;
                    return ProducerIntervalResponse.builder()
                            .producer(producer)
                            .interval(interval)
                            .previousWin(previousWin)
                            .followingWin(followingWin)
                            .build();
                })
                .toList();
    }

    private String generateImportId() {
        return UUID.randomUUID().toString();
    }
}
