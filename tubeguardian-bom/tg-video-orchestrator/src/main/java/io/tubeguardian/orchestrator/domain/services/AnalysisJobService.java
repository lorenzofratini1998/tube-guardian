package io.tubeguardian.orchestrator.domain.services;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.exception.JobNotFoundException;
import io.tubeguardian.orchestrator.repository.AnalysisJobRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnalysisJobService {

  private static final Logger log = LoggerFactory.getLogger(AnalysisJobService.class);

  private final AnalysisJobRepository analysisJobRepository;

  public AnalysisJobService(AnalysisJobRepository analysisJobRepository) {
    this.analysisJobRepository = analysisJobRepository;
  }

  public void markJobCompletion(UUID videoId, JobStatus status) {
    AnalysisJob job =
        analysisJobRepository
            .getAnalysisJobsByVideoId(videoId)
            .orElseThrow(() -> new JobNotFoundException(videoId));
    job.setStatus(JobStatus.COMPLETED);
    log.info("Marking job [{}] as [{}]", job.getId(), status.name());
    analysisJobRepository.save(job);
  }
}
