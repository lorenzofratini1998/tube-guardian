package io.tubeguardian.policyengine.domain.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import io.tubeguardian.common.domain.AnalysisResult.RiskProfile;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.VideoContent;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.common.repository.VideoContentRepository;
import io.tubeguardian.policyengine.domain.exception.AIProviderException;
import io.tubeguardian.policyengine.infrastructure.messaging.producer.AnalysisResultProducer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyEngineServiceTest {

  @Mock private VideoContentRepository videoContentRepository;
  @Mock private AnalysisResultRepository resultRepository;
  @Mock private AnalysisResultProducer resultProducer;
  @Mock private GarmAnalysisService garmAnalysisService;

  @InjectMocks private PolicyEngineService service;

  @Test
  @DisplayName("Happy Path: Should analyze, save result and notify COMPLETED")
  void processVideo_Success() {
    UUID videoId = UUID.randomUUID();

    VideoContent videoContent = mock(VideoContent.class);
    Video video = mock(Video.class);

    when(videoContent.getVideo()).thenReturn(video);
    when(videoContent.getTranscriptText()).thenReturn("Valid Transcript");
    when(videoContentRepository.findByVideoId(videoId)).thenReturn(Optional.of(videoContent));
    RiskProfile riskProfile = mock(RiskProfile.class);
    when(garmAnalysisService.analyzeTranscript("Valid Transcript")).thenReturn(riskProfile);

    service.processVideo(videoId);

    verify(resultRepository).save(any());

    verify(resultProducer)
        .sendCompletion(
            argThat(
                event -> event.videoId().equals(videoId) && event.status() == JobStatus.COMPLETED));
  }

  @Test
  @DisplayName("Business Error: Should notify FAILED and consume message when Video not found")
  void processVideo_VideoNotFound() {
    UUID videoId = UUID.randomUUID();
    when(videoContentRepository.findByVideoId(videoId)).thenReturn(Optional.empty());

    service.processVideo(videoId);

    verify(resultProducer).sendCompletion(argThat(event -> event.status() == JobStatus.FAILED));
    verify(resultRepository, never()).save(any());
  }

  @Test
  @DisplayName("Business Error: Should notify FAILED when Transcript is missing")
  void processVideo_TranscriptMissing() {
    UUID videoId = UUID.randomUUID();
    VideoContent videoContent = mock(VideoContent.class);

    when(videoContent.getTranscriptText()).thenReturn("");
    when(videoContent.getVideoId()).thenReturn(videoId);
    when(videoContentRepository.findByVideoId(videoId)).thenReturn(Optional.of(videoContent));

    service.processVideo(videoId);

    verify(resultProducer).sendCompletion(argThat(event -> event.status() == JobStatus.FAILED));
    verify(garmAnalysisService, never()).analyzeTranscript(any());
  }

  @Test
  @DisplayName("Technical Error: Should RETHROW exception to trigger Retry when AI fails")
  void processVideo_TechnicalError() {
    UUID videoId = UUID.randomUUID();
    VideoContent videoContent = mock(VideoContent.class);
    when(videoContent.getTranscriptText()).thenReturn("Valid Transcript");
    when(videoContentRepository.findByVideoId(videoId)).thenReturn(Optional.of(videoContent));

    when(garmAnalysisService.analyzeTranscript(anyString()))
        .thenThrow(new AIProviderException("AI Down", new RuntimeException()));

    assertThatThrownBy(() -> service.processVideo(videoId)).isInstanceOf(AIProviderException.class);

    verify(resultProducer, never()).sendCompletion(any());
  }
}
