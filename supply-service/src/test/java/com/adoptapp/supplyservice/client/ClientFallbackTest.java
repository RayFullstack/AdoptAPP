package com.adoptapp.supplyservice.client;

import com.adoptapp.supplyservice.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void dependencyFallbacks_shouldReturnServiceUnavailable() {
        NotificationRequest request = new NotificationRequest(1L, 2L, "shelter@mail.com", "Mensaje", "SUPPLY", "SENT");

        assertThat(new NotificationServiceClientFallback().sendNotification(request).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new ShelterServiceClientFallback().getShelterById(2L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new StaffServiceClientFallback().getStaffByUserId(1L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new UserServiceClientFallback().getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}