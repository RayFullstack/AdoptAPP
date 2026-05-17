package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        log.warn("User-service no disponible para userId {}", id);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<UserResponse> getUserByEmail(String email) {
        log.warn("User-service no disponible para email {}", email);
        return ResponseEntity.notFound().build();
    }
}
