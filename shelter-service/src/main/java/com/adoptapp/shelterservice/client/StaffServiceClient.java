package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.StaffResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "staff-service", url = "${services.staff-service.url}", fallback = StaffServiceClientFallback.class)
public interface StaffServiceClient {

    @GetMapping("/staff/by-user/{userId}")
    ResponseEntity<StaffResponse> getStaffByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/staff/internal/shelter/{shelterId}/active")
    ResponseEntity<List<StaffResponse>> getActiveStaffByShelter(@PathVariable("shelterId") Long shelterId);
}
