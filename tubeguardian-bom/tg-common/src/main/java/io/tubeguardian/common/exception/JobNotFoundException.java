package io.tubeguardian.common.exception;

import java.util.UUID;

public class JobNotFoundException extends RuntimeException {
  public JobNotFoundException(UUID jobId) {
    super(String.format("Job [%s] not found.", jobId));
  }
}
