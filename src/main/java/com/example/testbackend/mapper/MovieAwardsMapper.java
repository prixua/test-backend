package com.example.testbackend.mapper;

import com.example.testbackend.dto.response.ProducerIntervalResponse;
import com.example.testbackend.dto.response.SummarizedAwardsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MovieAwardsMapper {

    /**
     * Cria uma resposta vazia quando não há dados para análise
     */
    public SummarizedAwardsResponse createEmptyResponse() {
        return SummarizedAwardsResponse.builder()
                .min(List.of())
                .max(List.of())
                .build();
    }

    /**
     * Constrói a resposta final com os intervalos mínimos e máximos
     */
    public SummarizedAwardsResponse buildSummarizedResponse(List<ProducerIntervalResponse> allIntervals) {
        int minInterval = findMinInterval(allIntervals);
        int maxInterval = findMaxInterval(allIntervals);

        List<ProducerIntervalResponse> minIntervals = filterIntervalsByValue(allIntervals, minInterval);
        List<ProducerIntervalResponse> maxIntervals = filterIntervalsByValue(allIntervals, maxInterval);

        return SummarizedAwardsResponse.builder()
                .min(minIntervals)
                .max(maxIntervals)
                .build();
    }

    /**
     * Encontra o menor intervalo entre todos os intervalos calculados
     */
    private int findMinInterval(List<ProducerIntervalResponse> allIntervals) {
        return allIntervals.stream()
                .mapToInt(ProducerIntervalResponse::getInterval)
                .min()
                .orElse(0);
    }

    /**
     * Encontra o maior intervalo entre todos os intervalos calculados
     */
    private int findMaxInterval(List<ProducerIntervalResponse> allIntervals) {
        return allIntervals.stream()
                .mapToInt(ProducerIntervalResponse::getInterval)
                .max()
                .orElse(0);
    }

    /**
     * Filtra intervalos que possuem um valor específico
     */
    private List<ProducerIntervalResponse> filterIntervalsByValue(List<ProducerIntervalResponse> allIntervals, int targetInterval) {
        return allIntervals.stream()
                .filter(interval -> interval.getInterval().equals(targetInterval))
                .toList();
    }

}
