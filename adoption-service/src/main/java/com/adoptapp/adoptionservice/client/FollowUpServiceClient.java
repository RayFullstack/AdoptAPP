package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.FollowUpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "followup-service", url = "${services.followup-service.url}", fallback = FollowUpServiceClientFallback.class)
public interface FollowUpServiceClient {

    @PostMapping("/followups")
    void createFollowUp(@RequestBody FollowUpRequest request);
}
