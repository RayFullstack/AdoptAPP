package com.adoptapp.supplyservice.client;

import com.adoptapp.supplyservice.dto.ShelterResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shelter-service", url = "${services.shelter-service.url}", fallback = ShelterServiceClientFallback.class)
public interface ShelterServiceClient {

    @GetMapping("/shelters/by-id/{id}")
    ResponseEntity<ShelterResponse> getShelterById(@PathVariable("id") Long id);
}
