package com.adoptapp.sharedkernel.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ErrorResponseEntity {

    private ErrorResponseEntity() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> ResponseEntity<T> notFound(String message) {
        return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseFactory.notFound(message, currentPath()));
    }

    private static String currentPath() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRequestURI();
        }

        return null;
    }
}
