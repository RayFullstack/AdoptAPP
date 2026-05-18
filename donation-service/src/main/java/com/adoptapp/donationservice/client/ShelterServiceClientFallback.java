package com.adoptapp.donationservice.client;


import com.adoptapp.donationservice.dto.ShelterAuthResponse;
import com.adoptapp.donationservice.dto.ShelterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShelterServiceClientFallback implements ShelterServiceClient {

    @Override
    public ResponseEntity<ShelterResponse> getShelterById(Long id) {
        log.warn("Shelter-service no disponible para userId {}", id);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ShelterAuthResponse> getShelterAuthByEmail(String email) {
        log.warn("Shelter-service no disponible para email {}", email);
        return ResponseEntity.notFound().build();
    }
}
