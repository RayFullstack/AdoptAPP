package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.FollowUpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FollowUpServiceClientFallback implements FollowUpServiceClient {

    @Override
    public void createFollowUp(FollowUpRequest request) {
        log.warn("Fallback: No se pudo crear seguimiento en followup-service para mascota {}",
                request.petName());
    }
}
