package com.capstone.server.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/*
This auto handles any exceptions thrown in the service layer or DTOs and returns a timestamp,
the http status code, the message thrown with the exception, and a map of errors if from Jakarta
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // DTO validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ValidationErrorResponse response = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 code
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 code
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 401 code
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex) {

        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 403 code
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {

        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 404 code
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // helper function
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                message
        );

        return ResponseEntity.status(status).body(response);
    }
}