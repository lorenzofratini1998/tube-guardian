package io.tubeguardian.orchestrator.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.repository.AnalysisJobRepository;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.domain.model.YoutubeUrl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.tubeguardian.orchestrator.util.TestFixtures;

@ExtendWith(MockitoExtension.class)
class AnalysisOrchestrationServiceTest {

  @Mock private VideoIngestionAdapter videoIngestionAdapter;
  @Mock private AnalysisJobRepository jobRepository;
  @Mock private AnalysisResultRepository resultRepository;

  @InjectMocks private AnalysisOrchestrationService service;

  @Test
  @DisplayName("Scenario: Result Exists -> Return Completed Response (Smart Cache)")
  void shouldReturnCompletedResponse_WhenResultExists() {
    AnalysisRequest request = TestFixtures.validRequest();
    Video video = TestFixtures.videoEntity();
    AnalysisResult existingResult = TestFixtures.analysisResult(video);

    when(videoIngestionAdapter.getOrIngestVideo(any(YoutubeUrl.class))).thenReturn(video);
    when(resultRepository.findByVideoId(video.getId())).thenReturn(Optional.of(existingResult));

    AnalysisJobResponse response = service.analyzeVideo(request);

    assertThat(response.status()).isEqualTo(JobStatus.COMPLETED.name());
    assertThat(response.result()).isNotNull();
    verify(jobRepository, never()).save(any());
  }

  @Test
  @DisplayName("Scenario: Job Running -> Return Existing Job (Deduplication)")
  void shouldReturnExistingJob_WhenJobIsRunning() {
    AnalysisRequest request = TestFixtures.validRequest();
    Video video = TestFixtures.videoEntity();
    AnalysisJob runningJob = TestFixtures.runningJob(video);

    when(videoIngestionAdapter.getOrIngestVideo(any(YoutubeUrl.class))).thenReturn(video);
    when(resultRepository.findByVideoId(video.getId())).thenReturn(Optional.empty());
    when(jobRepository.findTopByVideoIdOrderByCreatedAtDesc(video.getId()))
        .thenReturn(Optional.of(runningJob));

    AnalysisJobResponse response = service.analyzeVideo(request);

    assertThat(response.status()).isEqualTo(JobStatus.PROCESSING.name());
    verify(jobRepository, never()).save(any());
  }

  @Test
  @DisplayName("Scenario: New Video -> Create New Job")
  void shouldCreateNewJob_WhenNoHistoryExists() {
    AnalysisRequest request = TestFixtures.validRequest();
    Video video = TestFixtures.videoEntity();
    AnalysisJob newJob = TestFixtures.pendingJob(video);

    when(videoIngestionAdapter.getOrIngestVideo(any(YoutubeUrl.class))).thenReturn(video);
    when(resultRepository.findByVideoId(video.getId())).thenReturn(Optional.empty());
    when(jobRepository.findTopByVideoIdOrderByCreatedAtDesc(video.getId()))
        .thenReturn(Optional.empty());

    when(jobRepository.save(any(AnalysisJob.class))).thenAnswer(i -> i.getArguments()[0]);

    AnalysisJobResponse response = service.analyzeVideo(request);

    assertThat(response.status()).isEqualTo(JobStatus.PENDING.name());
    verify(jobRepository).save(any(AnalysisJob.class));
  }
}
