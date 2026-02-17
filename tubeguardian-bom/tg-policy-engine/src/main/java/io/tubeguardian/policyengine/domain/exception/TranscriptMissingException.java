package io.tubeguardian.policyengine.domain.exception;

import io.tubeguardian.common.exception.TubeGuardianException;

import java.util.UUID;

public class TranscriptMissingException extends TubeGuardianException {
  public TranscriptMissingException(UUID videoId) {
    super(String.format("Video [%s] does not have a valid transcript available for analysis", videoId));
  }
}
