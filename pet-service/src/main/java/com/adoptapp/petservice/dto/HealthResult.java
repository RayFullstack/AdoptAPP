package com.adoptapp.petservice.dto;

public record HealthResult(
        Long id,
        Boolean vaccinated,
        Boolean sterilized,
        String diseases
) {
}
