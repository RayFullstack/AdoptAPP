package com.adoptapp.healthservice.controller;

import com.adoptapp.healthservice.dto.HealthResult;
import com.adoptapp.healthservice.model.HealthStatus;
import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import com.adoptapp.healthservice.service.HealthLinkAssembler;
import com.adoptapp.healthservice.service.HealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private HealthService service;

    private HealthController controller;

    @BeforeEach
    void setUp() {
        controller = new HealthController(service, new HealthLinkAssembler());
    }

    @Test
    void getHealthById_shouldReturnOk_whenHealthExists() {
        HealthResult result = new HealthResult(1L, 7L, 10L, VaccinationStatus.VACCINATED,
                SterilizationStatus.STERILIZED, "Sin enfermedades", HealthStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(service.getById(1L)).thenReturn(Optional.of(result));

        ResponseEntity<?> response = controller.getHealthById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getHealthById_shouldReturnNotFound_whenHealthDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getHealthById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}