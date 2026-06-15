package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.PetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "pet-service", url = "${services.pet-service.url}", fallback = PetServiceClientFallback.class)
public interface PetServiceClient {

    @GetMapping("/pets/internal/shelter/{shelterId}/active")
    ResponseEntity<List<PetResponse>> getActivePetsByShelter(@PathVariable("shelterId") Long shelterId);
}
