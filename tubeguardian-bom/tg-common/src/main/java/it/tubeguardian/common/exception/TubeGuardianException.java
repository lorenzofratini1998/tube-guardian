package it.tubeguardian.common.exception;

public abstract class TubeGuardianException extends RuntimeException {

    protected TubeGuardianException(String message) {
        super(message);
    }

    protected TubeGuardianException(String message, Throwable cause) {
        super(message, cause);
    }
}
