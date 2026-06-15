package com.adoptapp.petservice.client;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(String email) {
        log.warn("User-service no disponible para auth {}", email);
        return ResponseEntity.notFound().build();
    }
}
