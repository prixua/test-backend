package com.example.testbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResponse {

    private String uuidImported;
}
