package com.adoptapp.petservice.dto;

public record HealthRequest(
        Boolean vaccinated,
        Boolean sterilized,
        String diseases
) {
}
