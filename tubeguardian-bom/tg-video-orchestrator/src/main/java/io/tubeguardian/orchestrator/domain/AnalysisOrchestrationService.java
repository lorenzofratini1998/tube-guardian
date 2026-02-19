package io.tubeguardian.orchestrator.domain;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.common.exception.JobNotFoundException;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.domain.model.YoutubeUrl;
import io.tubeguardian.orchestrator.repository.AnalysisJobRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AnalysisOrchestrationService {

  private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrationService.class);

  private final VideoIngestionAdapter videoIngestionAdapter;
  private final AnalysisJobRepository jobRepository;
  private final AnalysisResultRepository resultRepository;
  private final ApplicationEventPublisher eventPublisher;

  public AnalysisOrchestrationService(
      VideoIngestionAdapter videoIngestionAdapter,
      AnalysisJobRepository jobRepository,
      AnalysisResultRepository resultRepository,
      ApplicationEventPublisher eventPublisher) {
    this.videoIngestionAdapter = videoIngestionAdapter;
    this.jobRepository = jobRepository;
    this.resultRepository = resultRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public AnalysisJobResponse analyzeVideo(AnalysisRequest request) {
    YoutubeUrl url = YoutubeUrl.of(request.url());
    log.info("Analyzing video [{}]", url.value());

    Video video = videoIngestionAdapter.getOrIngestVideo(url);

    Optional<AnalysisResult> existingResult = resultRepository.findByVideoId(video.getId());

    if (existingResult.isPresent()) {
      log.info(
          "Analysis Cache HIT for video [{}]. Returning existing result.", video.getYoutubeId());
      return AnalysisJobResponse.fromAnalysisResult(existingResult.get());
    }

    Optional<AnalysisJob> existingJob =
        jobRepository.findTopByVideoIdOrderByCreatedAtDesc(video.getId());
    if (existingJob.isPresent() && !existingJob.get().getStatus().isTerminal()) {
      log.info(
          "Job Deduplication: Found active job [{}] for video [{}].",
          existingJob.get().getId(),
          video.getYoutubeId());
      return AnalysisJobResponse.fromAnalysisJob(existingJob.get());
    }

    AnalysisJob newJob = jobRepository.save(AnalysisJob.create(video));
    log.info("Analysis job [{}] created for video [{}]", newJob.getId(), video.getYoutubeId());
    eventPublisher.publishEvent(AnalysisJobEvent.fromDomain(newJob));

    return AnalysisJobResponse.fromAnalysisJob(newJob);
  }

  public AnalysisJobResponse getJobStatus(UUID jobId) {
    AnalysisJob job =
        jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
    return AnalysisJobResponse.fromAnalysisJob(job);
  }
}
