package io.tubeguardian.common.domain.event;

import io.tubeguardian.common.domain.status.JobStatus;

import java.io.Serializable;
import java.util.UUID;

public record AnalysisResultEvent(
        UUID videoId,
        JobStatus status
) implements Serializable {}
