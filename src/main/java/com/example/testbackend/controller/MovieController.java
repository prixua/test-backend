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
import org.springframework.http.MediaType;
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
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> importCsv(MultipartFile file) {
        log.info("POST /api/v1/movies/import - importando arquivo CSV: {}", file.getOriginalFilename());

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
    @GetMapping("/import/{uuidImport}/awards")
    public ResponseEntity<SummarizedAwardsResponse> getSummarizedAwards(@PathVariable String uuidImport) {
        log.info("GET /api/v1/movies/import/{}/awards - obtendo análise de prêmios para UUID: {}", uuidImport, uuidImport);

        SummarizedAwardsResponse awards = movieService.getSummarizedAwards(uuidImport);
        return ResponseEntity.ok(awards);
    }

    @Override
    @GetMapping("/import/uuids")
    public ResponseEntity<List<String>> getImportUuids() {
        log.info("GET /api/v1/movies/import/uuids - obtendo UUIDs de importação");

        List<String> importUuids = movieService.getImportUuids();
        return ResponseEntity.ok(importUuids);
    }

    @Override
    @GetMapping("/import/{uuidImport}/list")
    public ResponseEntity<List<MovieResponse>> getMoviesByImportUuid(@PathVariable String uuidImport) {
        log.info("GET /api/v1/movies/import/{}/list - buscando filmes por UUID", uuidImport);

        List<MovieResponse> movies = movieService.getMoviesByImportUuid(uuidImport);
        return ResponseEntity.ok(movies);
    }
}
