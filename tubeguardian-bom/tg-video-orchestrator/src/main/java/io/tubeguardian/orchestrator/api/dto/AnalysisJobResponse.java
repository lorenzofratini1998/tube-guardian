package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.status.JobStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisJobResponse(
    UUID jobId, String videoId, String status, LocalDateTime createdAt, RiskProfileDto result) {

  public static AnalysisJobResponse fromAnalysisJob(AnalysisJob job) {
    return new AnalysisJobResponse(
        job.getId(),
        job.getVideo().getYoutubeId(),
        job.getStatus().name(),
        job.getCreatedAt(),
        null);
  }

  public static AnalysisJobResponse fromAnalysisResult(AnalysisResult result) {
    return new AnalysisJobResponse(
        null,
        result.getVideo().getYoutubeId(),
        JobStatus.COMPLETED.name(),
        result.getAnalyzedAt(),
        RiskProfileDto.fromDomain(result.getFullResponse()));
  }
}
