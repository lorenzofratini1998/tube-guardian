package io.tubeguardian.policyengine.domain.exception;

import io.tubeguardian.common.exception.TubeGuardianException;

public class AIProviderException extends TubeGuardianException {
  public AIProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
