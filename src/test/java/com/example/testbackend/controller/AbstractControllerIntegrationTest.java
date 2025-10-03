package com.example.testbackend.controller;

import com.example.testbackend.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public abstract class AbstractControllerIntegrationTest {

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        movieRepository.deleteAll();
    }

    /**
     * Método helper para importar arquivo CSV e retornar o MvcResult completo
     */
    protected MvcResult importCsvFileAndGetResult(String fileName) throws Exception {
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
}
