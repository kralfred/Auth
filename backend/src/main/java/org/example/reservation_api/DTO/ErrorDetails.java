package org.example.reservation_api.DTO;

public record ErrorDetails(
        String errorCode,
        String exceptionClass,
        String message
) {}