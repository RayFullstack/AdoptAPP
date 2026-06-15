package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.PetResponse;
import com.adoptapp.adoptionservice.dto.PetStatusRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pet-service", url = "${services.pet-service.url}", fallback = PetServiceClientFallback.class)
public interface PetServiceClient {

    @GetMapping("/pets/by-id/{id}")
    ResponseEntity<PetResponse> getPetById(@PathVariable("id") Long id);

    @PatchMapping("/pets/by-id/{id}/status")
    ResponseEntity<PetResponse> updatePetStatus(
            @PathVariable("id") Long id,
            @RequestBody PetStatusRequest request);

}
