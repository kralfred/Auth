package org.example.reservation_api.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.ErrorDetails;
import org.example.reservation_api.DTO.ErrorResponse;
import org.example.reservation_api.messages.UserNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private void captureErrorDetails(HttpServletRequest request, String errorCode, Exception ex, String message) {
        if (request != null) {
            String errorMessage = (message != null && !message.isBlank()) ? message : ex.getClass().getSimpleName();
            ErrorDetails errorDetails = new ErrorDetails(
                    errorCode,
                    ex.getClass().getName(),
                    errorMessage
            );

            // Set attribute for downstream handlers
            request.setAttribute("ERROR_DETAILS", errorDetails);

            // Optionally log error details directly
            log.error("Handled Exception [{}] for Path [{}]: {}", errorCode, request.getRequestURI(), errorMessage);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        captureErrorDetails(request, "INTERNAL_SERVER_ERROR", ex, ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred."));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        captureErrorDetails(request, "AUTH_001", ex, ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("AUTH_001", "Invalid credentials"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        captureErrorDetails(request, "INVALID_ARGUMENT", ex, ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        captureErrorDetails(request, "USER_NOT_FOUND", ex, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseError(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        // Extract the root cause message (e.g., PostgreSQL constraint details)
        String detailedMessage = (ex.getRootCause() != null) ? ex.getRootCause().getMessage() : ex.getMessage();

        captureErrorDetails(request, "DB_ERROR_001", ex, detailedMessage);

        ErrorResponse error = new ErrorResponse(
                "DB_ERROR_001",
                "Database communication error. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ErrorDetails> handleBadSqlGrammarException(BadSqlGrammarException ex, HttpServletRequest request) {
        // 1. Get the underlying database exception (Root Cause)
        Throwable rootCause = ex.getRootCause();
        String detailedMessage = (rootCause != null) ? rootCause.getMessage() : ex.getMessage();

        // 2. Find the exact class and line number in your application code where it failed
        StackTraceElement origin = getApplicationOrigin(ex);
        String locationInfo = (origin != null)
                ? origin.getClassName() + "." + origin.getMethodName() + "(line " + origin.getLineNumber() + ")"
                : "Unknown Location";

        // 3. Log the pinpointed error details clearly
        log.error("SQL Error occurred at: {}", locationInfo);
        log.error("Database Root Cause: {}", detailedMessage);

        ErrorDetails errorDetails = new ErrorDetails(
                "DB_ERROR_001",
                ex.getClass().getName(),
                "SQL Error at " + locationInfo + " -> " + detailedMessage
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    // Helper method to filter out Spring/JDBC stack trace frames and return your code's origin line
    private StackTraceElement getApplicationOrigin(Throwable ex) {
        for (StackTraceElement element : ex.getStackTrace()) {
            // Filter by your project's root package name
            if (element.getClassName().startsWith("org.example.reservation_api")) {
                return element;
            }
        }
        return null;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String field = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getField()
                : "unknown";
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation failed";

        String fullMessage = "Validation failed on field [" + field + "]: " + message;

        captureErrorDetails(request, "VALIDATION_ERROR", ex, fullMessage);

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", fullMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
