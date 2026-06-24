package com.adoptapp.shelterservice.controller;

import com.adoptapp.shelterservice.dto.ShelterResult;
import com.adoptapp.shelterservice.model.ShelterStatus;
import com.adoptapp.shelterservice.service.ShelterLinkAssembler;
import com.adoptapp.shelterservice.service.ShelterService;
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
class ShelterControllerTest {

    @Mock
    private ShelterService service;

    private ShelterController controller;

    @BeforeEach
    void setUp() {
        controller = new ShelterController(service, new ShelterLinkAssembler());
    }

    @Test
    void getShelterById_shouldReturnOk_whenShelterExists() {
        ShelterResult result = new ShelterResult(1L, "Refugio", "refugio@mail.com", "123456789",
                "Descripcion", ShelterStatus.ACTIVE, true, LocalDateTime.now(), LocalDateTime.now());
        when(service.getById(1L)).thenReturn(Optional.of(result));

        ResponseEntity<?> response = controller.getShelterById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShelterById_shouldReturnNotFound_whenShelterDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getShelterById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}