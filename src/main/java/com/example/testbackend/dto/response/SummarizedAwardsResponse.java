package com.example.testbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Análise resumida dos intervalos entre prêmios dos produtores")
public class SummarizedAwardsResponse {

    @Schema(
            description = "Lista de produtores com os menores intervalos entre prêmios consecutivos (mais rápido)",
            example = "[{\"producer\": \"Joel Silver\", \"interval\": 1, \"previousWin\": 1990, \"followingWin\": 1991}]"
    )
    private List<ProducerIntervalResponse> min;

    @Schema(
            description = "Lista de produtores com os maiores intervalos entre prêmios consecutivos (maior lacuna)",
            example = "[{\"producer\": \"Matthew Vaughn\", \"interval\": 13, \"previousWin\": 2002, \"followingWin\": 2015}]"
    )
    private List<ProducerIntervalResponse> max;
}
