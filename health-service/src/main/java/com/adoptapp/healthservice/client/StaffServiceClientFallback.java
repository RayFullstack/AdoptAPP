package com.adoptapp.healthservice.client;

import com.adoptapp.healthservice.dto.StaffResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class StaffServiceClientFallback implements StaffServiceClient {

    @Override
    public ResponseEntity<StaffResponse> getStaffByUserId(Long userId) {
        return ResponseEntity.status(503).build();
    }
}
