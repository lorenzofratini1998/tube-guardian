package it.tubeguardian.common.exception;

import it.tubeguardian.common.domain.status.JobStatus;

import java.util.UUID;

public class InvalidJobStateTransitionException extends TubeGuardianException {

    public InvalidJobStateTransitionException(UUID jobId, JobStatus currentStatus, JobStatus newStatus) {
        super(String.format(
                "Invalid state transition for Job [%s]: Cannot move from %s to %s because the job is already in a terminal state.",
                jobId, currentStatus, newStatus
        ));
    }
}
