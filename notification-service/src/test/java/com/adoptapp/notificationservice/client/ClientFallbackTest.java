package com.adoptapp.notificationservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void staffFallback_shouldReturnServiceUnavailable() {
        assertThat(new StaffServiceClientFallback().getStaffByUserId(1L).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void userFallback_shouldReturnServiceUnavailable() {
        assertThat(new UserServiceClientFallback().getUserAuthByEmail("user@mail.com").getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}