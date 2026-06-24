package com.adoptapp.userservice.client;

import com.adoptapp.userservice.dto.UserNotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void userNotificationFallback_shouldReturnServiceUnavailable() {
        UserNotificationRequest request = new UserNotificationRequest(1L, "user@mail.com", "Mensaje", "USER", "SENT");

        assertThat(new UserNotificationClientFallback().sendNotification(request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}