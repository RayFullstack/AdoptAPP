package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.HealthRequest;
import com.adoptapp.petservice.dto.HealthResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "health-service", url = "${services.health-service.url}", fallback = HealthServiceClientFallback.class)
public interface HealthServiceClient {

    @PostMapping("/api/health")
    ResponseEntity<HealthResult> createHealth(@RequestBody HealthRequest request);

    @GetMapping("/api/health/{id}")
    ResponseEntity<HealthResult> getHealth(@PathVariable("id") Long id);

    @PutMapping("/api/health/{id}")
    ResponseEntity<HealthResult> updateHealth(@PathVariable("id") Long id, @RequestBody HealthRequest request);

    @DeleteMapping("/api/health/{id}")
    ResponseEntity<Void> deleteHealth(@PathVariable("id") Long id);
}
