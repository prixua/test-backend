package com.example.testbackend.repository;

import com.example.testbackend.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByImportUuid(String importUuid);


    @Query("SELECT DISTINCT m.importUuid FROM Movie m ORDER BY m.createdAt DESC")
    List<String> findDistinctImportUuids();
}
