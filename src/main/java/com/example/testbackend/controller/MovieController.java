package com.example.testbackend.controller;

import com.example.testbackend.controller.api.MovieApi;
import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import com.example.testbackend.dto.response.ImportResponse;
import com.example.testbackend.dto.response.MovieResponse;
import com.example.testbackend.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MovieController implements MovieApi {

    private final MovieService movieService;

    @Override
    public ResponseEntity<ImportResponse> importCsv(MultipartFile file) {
        log.info("POST /api/v1/movies/import - importing CSV file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!"csv".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("Arquivo deve ser do tipo CSV");
        }

        ImportResponse response = movieService.importCsvFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<String>> getImportUuids() {
        log.info("GET /api/v1/movies/import-uuids");

        List<String> importUuids = movieService.getImportUuids();
        return ResponseEntity.ok(importUuids);
    }

    @Override
    public ResponseEntity<List<MovieResponse>> getMoviesByImportUuid(String importUuid) {
        log.info("GET /api/v1/movies/by-import/{}", importUuid);

        List<MovieResponse> movies = movieService.findByImportUuid(importUuid);
        return ResponseEntity.ok(movies);
    }

    @Override
    public ResponseEntity<SummarizedAwardsResponse> getSummarizedAwards(String uuid) {
        log.info("GET /api/v1/movies/awards - uuid: {}", uuid);

        SummarizedAwardsResponse awards = movieService.getAwardsByImportUuid(uuid);
        return ResponseEntity.ok(awards);
    }
}
