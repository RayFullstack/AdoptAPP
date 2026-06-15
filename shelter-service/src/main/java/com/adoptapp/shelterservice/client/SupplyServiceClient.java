package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.SupplyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "supply-service", url = "${services.supply-service.url}", fallback = SupplyServiceClientFallback.class)
public interface SupplyServiceClient {

    @GetMapping("/supplies/internal/shelter/{shelterId}/active")
    ResponseEntity<List<SupplyResponse>> getActiveSuppliesByShelter(@PathVariable("shelterId") Long shelterId);
}
