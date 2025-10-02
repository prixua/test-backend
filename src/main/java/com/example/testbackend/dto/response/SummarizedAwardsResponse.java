package com.example.testbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SummarizedAwardsResponse {

    private List<ProducerIntervalResponse> min;
    private List<ProducerIntervalResponse> max;
}
