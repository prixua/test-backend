package com.example.testbackend.config;

import com.example.testbackend.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MovieService movieService;

    @Override
    public void run(String... args) {
        loadInitialData();
    }

    private void loadInitialData() {
        try {
            log.info("Iniciando carregamento dos dados iniciais...");

            ClassPathResource resource = new ClassPathResource("input/movielist.csv");

            if (!resource.exists()) {
                log.warn("Arquivo movielist.csv não encontrado em resources/input/");
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                // Criar um MultipartFile personalizado para o carregamento inicial
                byte[] fileContent = inputStream.readAllBytes();
                MultipartFile multipartFile = new InitialDataMultipartFile("movielist.csv", fileContent);

                var response = movieService.importCsvFile(multipartFile);
                log.info("Dados iniciais carregados com sucesso. UUID da importação: {}", response.getUuidImported());

            } catch (IOException e) {
                log.error("Erro ao ler arquivo movielist.csv: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Erro ao carregar dados iniciais: {}", e.getMessage(), e);
        }
    }

    // Classe interna para implementar MultipartFile para dados iniciais
    private static class InitialDataMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        public InitialDataMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return "text/csv";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo not supported for initial data loading");
        }
    }
}
