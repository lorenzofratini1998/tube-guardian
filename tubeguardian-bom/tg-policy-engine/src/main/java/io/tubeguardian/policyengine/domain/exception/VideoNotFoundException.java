package io.tubeguardian.policyengine.domain.exception;

import io.tubeguardian.common.exception.TubeGuardianException;
import java.util.UUID;

public class VideoNotFoundException extends TubeGuardianException {
  public VideoNotFoundException(UUID videoId) {
    super(String.format("Video with ID [%s] not found in database.", videoId));
  }
}
