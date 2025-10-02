package com.example.testbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MovieResponse {

    private Long id;
    private Integer year;
    private String title;
    private String studios;
    private String producers;
    private Boolean winner;
    private String importUuid;
    private LocalDateTime createdAt;
}
