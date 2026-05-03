package com.adoptapp.petservice.dto;

import java.time.LocalDateTime;

public record ErrorResponse (
        String message,
        int status,
        LocalDateTime timestamp){
}
