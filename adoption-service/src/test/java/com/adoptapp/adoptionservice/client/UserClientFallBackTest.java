package com.adoptapp.adoptionservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserClientFallBackTest {

    private final UserServiceClientFallback fallback = new UserServiceClientFallback();

    @Test
    void getUserById_shouldReturnNotFound_whenUserServiceIsUnavailable() {
        assertThat(fallback.getUserById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserAuthByEmail_shouldReturnNotFound_whenUserServiceIsUnavailable() {
        assertThat(fallback.getUserAuthByEmail("user@mail.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}