package io.tubeguardian.orchestrator.api.exception;

import io.tubeguardian.common.exception.ServiceUnavailableException;
import io.tubeguardian.orchestrator.api.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));

    log.warn("Validation failed: {}", errorMessages);

    return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", errorMessages, request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), request);
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ApiErrorResponse> handleServiceUnavailable(
      ServiceUnavailableException ex, HttpServletRequest request) {
    log.error("Dependency failure: {}", ex.getMessage());
    return buildResponse(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        "The video ingestion service is currently down. Please try again later.",
        request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error occurred", ex);
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "An unexpected error occurred.",
        request);
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status, String error, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(ApiErrorResponse.of(status.value(), error, message, request.getRequestURI()));
  }
}
