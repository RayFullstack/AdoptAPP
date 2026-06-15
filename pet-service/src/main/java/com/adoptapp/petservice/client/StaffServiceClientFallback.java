package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.StaffResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StaffServiceClientFallback implements StaffServiceClient {

    @Override
    public ResponseEntity<StaffResponse> getStaffByUserId(Long userId) {
        log.warn("Staff-service no disponible para userId {}", userId);
        return ResponseEntity.notFound().build();
    }
}
