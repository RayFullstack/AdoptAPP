package com.adoptapp.staffservice.client;

import com.adoptapp.staffservice.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void notificationFallback_shouldReturnServiceUnavailable() {
        NotificationRequest request = new NotificationRequest(1L, 2L, "staff@mail.com", "Mensaje", "STAFF", "SENT");

        assertThat(new NotificationServiceClientFallback().sendNotification(request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void remoteLookupFallbacks_shouldReturnNotFound() {
        assertThat(new ShelterServiceClientFallback().getShelterById(2L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}