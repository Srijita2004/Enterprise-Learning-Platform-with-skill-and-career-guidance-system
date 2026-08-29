package com.skillspherenexus.certificationmanagementservice.exception;

import com.skillspherenexus.certificationmanagementservice.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiErrorResponse> duplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiErrorResponse> external(ExternalServiceException ex, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(ForbiddenOperationException.class)
    ResponseEntity<ApiErrorResponse> forbidden(ForbiddenOperationException ex, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiErrorResponse> badRequest(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String,String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Request validation failed", request, errors);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", request, Map.of());
    }
    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message, HttpServletRequest request, Map<String,String> errors) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), errors));
    }
}
