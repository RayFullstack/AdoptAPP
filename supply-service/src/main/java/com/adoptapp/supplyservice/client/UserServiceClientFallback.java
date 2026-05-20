package com.adoptapp.supplyservice.client;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.supplyservice.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(String email) {
        log.error("Fallback: user-service no disponible, email={}", email);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        log.error("Fallback: user-service no disponible, userId={}", id);
        return ResponseEntity.status(503).build();
    }
}
