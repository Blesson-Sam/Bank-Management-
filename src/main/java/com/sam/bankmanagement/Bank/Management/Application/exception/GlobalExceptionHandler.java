package com.sam.bankmanagement.Bank.Management.Application.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handleInsufficientFundsException(
            InsufficientFundsException ex, HttpServletRequest request) {

        log.error("Insufficient funds: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Insufficient Funds")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiError> handleAccountStatusException(
            AccountStatusException ex, HttpServletRequest request) {

        log.error("Account status error: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Account Status Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.error("Duplicate resource: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Resource")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        List<ApiError.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();

            validationErrors.add(ApiError.ValidationError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String rootCause = ex.getCause().getMessage();
            if (rootCause.contains("unique constraint") || rootCause.contains("duplicate key")) {
                message = "Duplicate entry found";
            }
        }

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Error")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(ActiveAccountsExistException.class)
    public ResponseEntity<ApiError> handleActiveAccounts(
            ActiveAccountsExistException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Active Accounts Exist")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.error("Method argument type mismatch: {}", ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Parameter")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ApiError> handleDailyLimit(DailyLimitExceededException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Daily Limit Exceeded")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: ", ex);

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
