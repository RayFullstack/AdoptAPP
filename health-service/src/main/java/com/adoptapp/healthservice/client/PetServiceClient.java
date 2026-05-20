package com.adoptapp.healthservice.client;

import com.adoptapp.healthservice.dto.PetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pet-service", url = "${services.pet-service.url}", fallback = PetServiceClientFallback.class)
public interface PetServiceClient {

    @GetMapping("/pets/by-id/{id}")
    ResponseEntity<PetResponse> getPetById(@PathVariable("id") Long id);
}
