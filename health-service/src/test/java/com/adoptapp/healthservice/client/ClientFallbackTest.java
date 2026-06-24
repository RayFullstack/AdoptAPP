package com.adoptapp.healthservice.client;

import com.adoptapp.healthservice.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void notificationFallback_shouldReturnServiceUnavailable() {
        NotificationRequest request = new NotificationRequest(1L, null, "user@mail.com", "Mensaje", "HEALTH", "SENT");

        assertThat(new NotificationServiceClientFallback().sendNotification(request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void remoteLookupFallbacks_shouldReturnExpectedStatusCodes() {
        assertThat(new PetServiceClientFallback().getPetById(10L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new StaffServiceClientFallback().getStaffByUserId(1L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new UserServiceClientFallback().getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}