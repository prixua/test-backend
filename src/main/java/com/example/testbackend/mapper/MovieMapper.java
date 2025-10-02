package com.example.testbackend.mapper;

import com.example.testbackend.dto.response.MovieResponse;
import com.example.testbackend.model.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .year(movie.getYear())
                .title(movie.getTitle())
                .studios(movie.getStudios())
                .producers(movie.getProducers())
                .winner(movie.getWinner())
                .importUuid(movie.getImportUuid())
                .createdAt(movie.getCreatedAt())
                .build();
    }
}
