package com.adoptapp.supplyservice.client;

import com.adoptapp.supplyservice.dto.UserAuthResponse;
import com.adoptapp.supplyservice.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(String email) {
        log.error("Fallback: No se pudo obtener usuario por email: {}", email);
        return null;
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        log.error("Fallback: No se pudo obtener usuario por id: {}", id);
        return null;
    }
}
