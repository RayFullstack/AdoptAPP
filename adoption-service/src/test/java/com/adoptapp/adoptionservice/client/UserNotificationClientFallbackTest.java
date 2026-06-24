package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.UserNotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotificationClientFallbackTest {

    private final UserNotificationClientFallback fallback =
            new UserNotificationClientFallback();

    @Test
    void sendNotification_shouldReturn503_whenNotificationServiceIsUnavailable() {
        // given
        UserNotificationRequest request =
                new UserNotificationRequest(
                        1L,
                        "adopter@mail.com",
                        "Adopcion creada",
                        "ADOPTION_CREATED",
                        "SENT"
                );

        // when
        ResponseEntity<Void> response =
                fallback.sendNotification(request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNull();
    }
}