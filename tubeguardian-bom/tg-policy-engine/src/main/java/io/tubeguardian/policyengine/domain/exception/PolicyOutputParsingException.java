package io.tubeguardian.policyengine.domain.exception;

import io.tubeguardian.common.exception.TubeGuardianException;

public class PolicyOutputParsingException extends TubeGuardianException {
  public PolicyOutputParsingException(String rawOutput, Throwable cause) {
    super(
        String.format("Failed to parse AI response into RiskProfile. Raw output: [%s]", rawOutput),
        cause);
  }
}
