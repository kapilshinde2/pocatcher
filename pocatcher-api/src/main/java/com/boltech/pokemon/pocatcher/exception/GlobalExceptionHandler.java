package com.boltech.pokemon.pocatcher.exception;

import java.time.LocalDateTime;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ApiError {
    private String message;
    private HttpStatus status;
    private LocalDateTime timestamp;
}

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return new ResponseEntity<>(
                new ApiError(message, HttpStatus.BAD_REQUEST, LocalDateTime.now()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CatalogFetchInterruptedException.class)
    public ResponseEntity<ApiError> handleCatalogInterrupted(CatalogFetchInterruptedException ex) {
        return new ResponseEntity<>(
                new ApiError(
                        "Catalog synchronization was interrupted",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        LocalDateTime.now()),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiError> handleRestClientResponse(RestClientResponseException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    new ApiError(ex.getMessage(), HttpStatus.BAD_GATEWAY, LocalDateTime.now()));
        }
        HttpStatus responseStats = status.is5xxServerError() ? HttpStatus.BAD_GATEWAY : status;
        return new ResponseEntity<>(
                new ApiError(ex.getMessage(), responseStats, LocalDateTime.now()),
                responseStats);
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return new ResponseEntity<>(
                new ApiError(reason, status, LocalDateTime.now()),
                status);
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<ApiError> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        return new ResponseEntity<>(
                new ApiError("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
