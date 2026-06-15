package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.StaffResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffServiceClientFallback implements StaffServiceClient {

    @Override
    public ResponseEntity<StaffResponse> getStaffByUserId(Long userId) {
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<List<StaffResponse>> getActiveStaffByShelter(Long shelterId) {
        return ResponseEntity.status(503).build();
    }
}
