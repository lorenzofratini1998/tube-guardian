package io.tubeguardian.common.exception;

import java.util.UUID;

public class VideoNotFoundException extends TubeGuardianException {
  public VideoNotFoundException(UUID videoId) {
    super(String.format("Video with ID [%s] not found in database.", videoId));
  }
}
