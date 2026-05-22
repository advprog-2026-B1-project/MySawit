package com.b1.mysawit.harvest.exception;

import com.b1.mysawit.harvest.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice(basePackages = "com.b1.mysawit.harvest")
public class HarvestExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        HttpStatus status = resolveStatus(exception.getMessage());
        return ResponseEntity.status(status).body(buildErrorResponse(status, exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException exception) {
        HttpStatus status = resolveStatus(exception.getMessage());
        return ResponseEntity.status(status).body(buildErrorResponse(status, exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage()));
    }

    private HttpStatus resolveStatus(String message) {
        if (message == null || message.isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }

        String normalizedMessage = message.toLowerCase(Locale.ROOT);

        if (normalizedMessage.contains("authentication required")) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (normalizedMessage.contains("could not extract email")) {
            return HttpStatus.BAD_REQUEST;
        }

        if (normalizedMessage.contains("not found") || normalizedMessage.contains("tidak ditemukan")) {
            return HttpStatus.NOT_FOUND;
        }

        if (normalizedMessage.contains("akses") || normalizedMessage.contains("hanya mandor")) {
            return HttpStatus.FORBIDDEN;
        }

        if (normalizedMessage.contains("sudah di approve")
                || normalizedMessage.contains("hanya dapat melaporkan hasil sekali sehari")) {
            return HttpStatus.CONFLICT;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}