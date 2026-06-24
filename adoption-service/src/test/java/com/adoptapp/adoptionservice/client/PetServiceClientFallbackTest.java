package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.PetResponse;
import com.adoptapp.adoptionservice.dto.PetStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PetServiceClientFallbackTest {

    private final PetServiceClientFallback fallback =
            new PetServiceClientFallback();

    @Test
    void getPetById_shouldReturn404_whenPetServiceIsUnavailable() {
        // given
        Long petId = 10L;

        // when
        ResponseEntity<PetResponse> response =
                fallback.getPetById(petId);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void updatePetStatus_shouldReturn503_whenPetServiceIsUnavailable() {
        // given
        Long petId = 10L;
        PetStatusRequest request =
                new PetStatusRequest("NOT_AVAILABLE");

        // when
        ResponseEntity<PetResponse> response =
                fallback.updatePetStatus(petId, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNull();
    }
}