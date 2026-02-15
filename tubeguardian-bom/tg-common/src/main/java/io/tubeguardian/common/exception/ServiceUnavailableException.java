package io.tubeguardian.common.exception;

public class ServiceUnavailableException extends RuntimeException {

  public ServiceUnavailableException(String serviceName) {
    super(String.format("Service [%s] is down or unreachable.", serviceName));
  }
}
