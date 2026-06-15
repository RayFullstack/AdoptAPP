package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.StaffResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "staff-service", url = "${services.staff-service.url}", fallback = StaffServiceClientFallback.class)
public interface StaffServiceClient {

    @GetMapping("/staff/by-user/{userId}")
    ResponseEntity<StaffResponse> getStaffByUserId(@PathVariable("userId") Long userId);
}
