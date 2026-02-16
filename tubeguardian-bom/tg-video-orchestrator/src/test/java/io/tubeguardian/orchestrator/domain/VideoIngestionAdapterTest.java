package io.tubeguardian.orchestrator.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.tubeguardian.common.domain.Video;
import io.tubeguardian.orchestrator.repository.VideoRepository;
import io.tubeguardian.orchestrator.domain.model.YoutubeUrl;
import io.tubeguardian.orchestrator.domain.services.VideoPersistenceService;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.VideoServiceClient;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.dto.VideoResponseDto;
import io.tubeguardian.orchestrator.util.TestFixtures;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VideoIngestionAdapterTest {

  @Mock private VideoRepository videoRepository;
  @Mock private VideoServiceClient videoServiceClient;
  @Mock private VideoPersistenceService videoPersistenceService;

  @InjectMocks private VideoIngestionAdapter adapter;

  @Test
  @DisplayName("Cache HIT: Should return video from DB without calling external service")
  void shouldReturnVideoFromDb_WhenExists() {
    YoutubeUrl url = TestFixtures.youtubeUrl();
    Video existingVideo = TestFixtures.videoEntity();

    when(videoRepository.findByYoutubeId(url.videoId())).thenReturn(Optional.of(existingVideo));

    Video result = adapter.getOrIngestVideo(url);

    assertThat(result).isEqualTo(existingVideo);
    verify(videoServiceClient, never()).fetchVideoData(any());
    verify(videoPersistenceService, never()).saveVideoData(any());
  }

  @Test
  @DisplayName("Cache MISS: Should fetch from service and save to DB")
  void shouldFetchAndSave_WhenDoesNotExist() {
    YoutubeUrl url = TestFixtures.youtubeUrl();
    VideoResponseDto responseDto = TestFixtures.videoResponseDto();
    Video savedVideo = TestFixtures.videoEntity();

    when(videoRepository.findByYoutubeId(url.videoId())).thenReturn(Optional.empty());
    when(videoServiceClient.fetchVideoData(url.value())).thenReturn(responseDto);
    when(videoPersistenceService.saveVideoData(responseDto)).thenReturn(savedVideo);

    Video result = adapter.getOrIngestVideo(url);

    assertThat(result).isEqualTo(savedVideo);
    verify(videoServiceClient).fetchVideoData(url.value());
    verify(videoPersistenceService).saveVideoData(responseDto);
  }
}
