package com.example.testbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server")
                ))
                .info(new Info()
                        .title("Movies API")
                        .description("API REST para gerenciamento de filmes e análise de prêmios cinematográficos. " +
                                   "Permite importar dados de filmes via CSV e realizar análises sobre intervalos " +
                                   "entre prêmios consecutivos dos produtores.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Test Backend")
                                .email("prixua@gmail.com")
                                .url("https://github.com/prixua/test-backend"))
                );
    }
}
