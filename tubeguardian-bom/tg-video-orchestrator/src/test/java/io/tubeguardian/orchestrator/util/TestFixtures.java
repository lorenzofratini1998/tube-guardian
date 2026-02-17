package io.tubeguardian.orchestrator.util;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.risk.RiskLevel;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.domain.model.YoutubeUrl;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.dto.VideoResponseDto;
import java.util.Map;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class TestFixtures {

  public static final String VALID_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
  public static final String VIDEO_ID = "dQw4w9WgXcQ";

  public static AnalysisRequest validRequest() {
    return new AnalysisRequest(VALID_URL);
  }

  public static VideoResponseDto videoResponseDto() {
    var metadata =
        new VideoResponseDto.VideoMetadataDto(
            VIDEO_ID,
            "Never Gonna Give You Up",
            "RickAstley",
            VALID_URL,
            "http://thumb.url",
            212,
            "20091025");
    var transcript =
        new VideoResponseDto.VideoTranscriptDto("We're no strangers to love...", "en", false);
    return new VideoResponseDto(metadata, transcript, "success");
  }

  public static Video videoEntity() {
      return Video.create(VIDEO_ID, "Test Video", "Test Channel");
  }

  public static Video videoEntityWithId() {
    Video video = Video.create(VIDEO_ID, "Test Video", "Test Channel");
    ReflectionTestUtils.setField(video, "id", UUID.randomUUID());
    return video;
  }

  public static AnalysisJob pendingJob(Video video) {
    AnalysisJob job = AnalysisJob.create(video);
    ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
    return job;
  }

  public static AnalysisJob runningJob(Video video) {
    AnalysisJob job = AnalysisJob.create(video);
    job.setStatus(JobStatus.PROCESSING);
    return job;
  }

  public static AnalysisResult analysisResult(Video video) {
    var riskProfile = new AnalysisResult.RiskProfile(RiskLevel.LOW, 95, "Safe content", Map.of());
    return AnalysisResult.create(video, riskProfile);
  }

  public static YoutubeUrl youtubeUrl() {
    return YoutubeUrl.of(VALID_URL);
  }
}
