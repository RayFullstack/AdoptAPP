package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void notificationFallback_shouldReturnServiceUnavailable() {
        NotificationRequest request = new NotificationRequest(1L, 2L, "shelter@mail.com", "Mensaje", "SHELTER", "SENT");

        assertThat(new NotificationServiceClientFallback().sendNotification(request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void dependencyFallbacks_shouldReturnServiceUnavailable() {
        assertThat(new PetServiceClientFallback().getActivePetsByShelter(2L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new StaffServiceClientFallback().getStaffByUserId(1L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new StaffServiceClientFallback().getActiveStaffByShelter(2L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(new SupplyServiceClientFallback().getActiveSuppliesByShelter(2L).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void userFallback_shouldReturnNotFound() {
        assertThat(new UserServiceClientFallback().getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}