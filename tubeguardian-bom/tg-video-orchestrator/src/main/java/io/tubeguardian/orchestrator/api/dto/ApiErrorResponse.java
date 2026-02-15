package io.tubeguardian.orchestrator.api.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    LocalDateTime timestamp, int status, String error, String message, String path) {
  public static ApiErrorResponse of(int status, String error, String message, String path) {
    return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path);
  }
}
