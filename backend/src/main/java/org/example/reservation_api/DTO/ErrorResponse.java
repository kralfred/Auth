package org.example.reservation_api.DTO;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String errorCode
) {}