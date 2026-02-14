package it.tubeguardian.common.domain.status;

public enum JobStatus {

    /**
     * The job has been created and persisted, but not yet picked up by a worker.
     */
    PENDING("Queued", false),

    /**
     * A worker has consumed the message and is actively processing the video.
     */
    PROCESSING("Processing", false),

    /**
     * The analysis completed successfully.
     * This is a terminal state.
     */
    COMPLETED("Completed", true),

    /**
     * The analysis failed due to an error (after retries) or was moved to the DLQ.
     * This is a terminal state.
     */
    FAILED("Failed", true);

    private final String description;
    private final boolean terminal;

    JobStatus(String description, boolean terminal) {
        this.description = description;
        this.terminal = terminal;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if the job has reached a final state (success or failure).
     * Useful for the FE to determine when to stop polling.
     * @return true if no further changes are expected
     */
    public boolean isTerminal() {
        return terminal;
    }
}
