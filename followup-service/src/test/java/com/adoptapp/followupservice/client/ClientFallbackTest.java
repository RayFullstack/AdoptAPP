package com.adoptapp.followupservice.client;

import com.adoptapp.followupservice.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void notificationFallback_shouldReturnServiceUnavailable() {
        NotificationRequest request = new NotificationRequest(1L, 2L, "user@mail.com", "Mensaje", "FOLLOWUP", "SENT");

        assertThat(new NotificationServiceClientFallback().sendNotification(request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void remoteLookupFallbacks_shouldReturnNotFound() {
        assertThat(new PetServiceClientFallback().getPetById(10L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}