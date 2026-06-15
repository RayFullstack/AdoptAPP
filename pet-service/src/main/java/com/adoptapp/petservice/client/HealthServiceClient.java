package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.HealthResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "health-service", url = "${services.health-service.url}", fallback = HealthServiceClientFallback.class)
public interface HealthServiceClient {

    @GetMapping("/health/by-pet/{petId}")
    ResponseEntity<HealthResult> getHealthByPetId(@PathVariable("petId") Long petId);

    @DeleteMapping("/health/by-pet/{petId}")
    ResponseEntity<Void> deleteHealthByPetId(@PathVariable("petId") Long petId);
}