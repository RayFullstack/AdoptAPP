package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ClientFallbackTest {

    @Test
    void petServiceFallback_shouldReturnExpectedStatusCodes() {
        PetServiceClientFallback fallback = new PetServiceClientFallback();

        assertThat(fallback.getPetById(10L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(fallback.updatePetStatus(10L, new PetStatusRequest("AVAILABLE")).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void notificationFallbacks_shouldReturnServiceUnavailable() {
        assertThat(new UserNotificationClientFallback().sendNotification(userNotification()).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new PetNotificationClientFallback().sendNotification(petNotification()).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void remoteLookupFallbacks_shouldReturnNotFound() {
        assertThat(new UserServiceClientFallback().getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new ShelterServiceClientFallback().getShelterById(2L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new StaffServiceClientFallback().getStaffByUserId(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void followUpFallback_shouldNotThrow() {
        FollowUpRequest request = new FollowUpRequest("Camila", "Benito", 1L, 10L, 5L,
                LocalDateTime.now(), "Seguimiento", "PENDING");

        assertThatCode(() -> new FollowUpServiceClientFallback().createFollowUp(request)).doesNotThrowAnyException();
    }

    private UserNotificationRequest userNotification() {
        return new UserNotificationRequest(1L, "user@mail.com", "Mensaje", "ADOPTION_CREATED", "SENT");
    }

    private PetNotificationRequest petNotification() {
        return new PetNotificationRequest(1L, "pet@mail.com", "Mensaje", "PET_UPDATED", "SENT");
    }
}