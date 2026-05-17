package com.adoptapp.petservice.dto;

import java.util.Map;

public record NotificationRequest(
        Long userId,
        String recipient,
        String typeName,
        Map<String, String> params
) {
}
