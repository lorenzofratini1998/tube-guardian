package io.tubeguardian.orchestrator.api.exception;

import io.tubeguardian.common.exception.TubeGuardianException;

public class BrandNotFoundException extends TubeGuardianException {
  public BrandNotFoundException(String brandProfileId) {
    super(String.format("Brand profile with ID [%s] does not exist.", brandProfileId));
  }
}
