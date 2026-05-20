package com.adoptapp.donationservice.client;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.donationservice.dto.UserResponse;
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
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(String email) {
        log.warn("User-service no disponible para email {}", email);
        return ResponseEntity.notFound().build();
    }
}