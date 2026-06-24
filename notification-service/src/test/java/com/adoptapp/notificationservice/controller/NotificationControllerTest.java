package com.adoptapp.notificationservice.controller;

import com.adoptapp.notificationservice.dto.NotificationResult;
import com.adoptapp.notificationservice.model.NotificationStatus;
import com.adoptapp.notificationservice.service.NotificationLinkAssembler;
import com.adoptapp.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService service;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(service, new NotificationLinkAssembler());
    }

    @Test
    void getNotificationById_shouldReturnOk_whenNotificationExists() {
        NotificationResult result = new NotificationResult(1L, 7L, null, "user@mail.com", "Mensaje",
                2L, "USER_CREATED", NotificationStatus.SENT, LocalDateTime.now());
        when(service.getByIdIncludingArchived(1L)).thenReturn(Optional.of(result));

        ResponseEntity<?> response = controller.getNotificationById(1L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getNotificationById_shouldReturnNotFound_whenNotificationDoesNotExist() {
        when(service.getByIdIncludingArchived(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getNotificationById(99L, admin());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Authentication admin() {
        return new TestingAuthenticationToken("admin@mail.com", "password", "ROLE_ADMIN");
    }
}