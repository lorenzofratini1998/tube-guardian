package io.tubeguardian.policyengine.domain.services;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.VideoContent;
import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.common.repository.VideoContentRepository;
import io.tubeguardian.policyengine.domain.exception.TranscriptMissingException;
import io.tubeguardian.common.exception.VideoNotFoundException;
import io.tubeguardian.policyengine.infrastructure.messaging.producer.AnalysisResultProducer;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PolicyEngineService {

  private static final Logger log = LoggerFactory.getLogger(PolicyEngineService.class);

  private final VideoContentRepository videoContentRepository;
  private final AnalysisResultRepository resultRepository;
  private final AnalysisResultProducer resultProducer;
  private final GarmAnalysisService garmAnalysisService;

  public PolicyEngineService(
      VideoContentRepository videoContentRepository,
      AnalysisResultRepository resultRepository,
      AnalysisResultProducer resultProducer,
      GarmAnalysisService garmAnalysisService) {
    this.videoContentRepository = videoContentRepository;
    this.resultRepository = resultRepository;
    this.resultProducer = resultProducer;
    this.garmAnalysisService = garmAnalysisService;
  }

  @Transactional
  public void processVideo(UUID videoId) {
    log.info("Orchestrating analysis for Video [{}]", videoId);

    try {
      VideoContent video =
          videoContentRepository
              .findByVideoId(videoId)
              .orElseThrow(() -> new VideoNotFoundException(videoId));

      String transcript = validateAndGetTranscript(video);
      AnalysisResult.RiskProfile riskProfile = garmAnalysisService.analyzeTranscript(transcript);
      saveResult(video, riskProfile);
      notifyCompletion(videoId, JobStatus.COMPLETED);

    } catch (VideoNotFoundException | TranscriptMissingException e) {
      log.error("Fatal business error: {}", e.getMessage());
      notifyCompletion(videoId, JobStatus.FAILED);
    } catch (Exception e) {
      log.error("Technical error processing video [{}]", videoId, e);
      throw e;
    }
  }

  private String validateAndGetTranscript(VideoContent videoContent) {
    String text = videoContent.getTranscriptText();
    if (text == null || text.isBlank()) {
      throw new TranscriptMissingException(videoContent.getVideoId());
    }
    return text;
  }

  private void saveResult(VideoContent videoContent, AnalysisResult.RiskProfile riskProfile) {
    AnalysisResult analysisResult = AnalysisResult.create(videoContent.getVideo(), riskProfile);
    resultRepository.save(analysisResult);
  }

  private void notifyCompletion(UUID videoId, JobStatus status) {
    resultProducer.sendCompletion(new AnalysisResultEvent(videoId, status));
  }
}
