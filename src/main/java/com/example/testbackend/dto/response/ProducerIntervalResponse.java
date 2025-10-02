package com.example.testbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Informações sobre o intervalo entre prêmios consecutivos de um produtor")
public class ProducerIntervalResponse {

    @Schema(description = "Nome do produtor", example = "Joel Silver")
    private String producer;

    @Schema(description = "Intervalo em anos entre os dois prêmios", example = "1")
    private Integer interval;

    @Schema(description = "Ano do prêmio anterior", example = "1990")
    private Integer previousWin;

    @Schema(description = "Ano do prêmio seguinte", example = "1991")
    private Integer followingWin;
}
