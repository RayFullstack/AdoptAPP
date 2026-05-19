package com.adoptapp.supplyservice.client;

import com.adoptapp.supplyservice.dto.UserAuthResponse;
import com.adoptapp.supplyservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service.url}", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/users/by-email/{email}/auth")
    ResponseEntity<UserAuthResponse> getUserAuthByEmail(@PathVariable("email") String email);

    @GetMapping("/users/by-id/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id);
}
