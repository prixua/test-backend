package com.example.testbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProducerIntervalResponse {

    private String producer;
    private Integer interval;
    private Integer previousWin;
    private Integer followingWin;
}
