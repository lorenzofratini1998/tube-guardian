package io.tubeguardian.orchestrator.domain;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.VideoContent;
import io.tubeguardian.orchestrator.AbstractIntegrationTest;
import io.tubeguardian.orchestrator.domain.model.YoutubeUrl;
import io.tubeguardian.orchestrator.repository.VideoRepository;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

class VideoIngestionAdapterIT extends AbstractIntegrationTest {

  @Autowired private VideoIngestionAdapter ingestionAdapter;
  @Autowired private VideoRepository videoRepository;

  @BeforeEach
  void setup() {
    videoRepository.deleteAll();
  }

  @Test
  @DisplayName(
      "Should fetch from Transcript Ingestion (WireMock) and persist to Postgres when cache MISS")
  void shouldIngestVideoOnCacheMiss() {
    String videoId = "dQw4w9WgXcQ";
    String url = "https://www.youtube.com/watch?v=" + videoId;

    stubIngestionResponse(videoId);

    Video result = ingestionAdapter.getOrIngestVideo(YoutubeUrl.of(url));

    assertThat(result.getYoutubeId()).isEqualTo(videoId);
    assertThat(result.getContent().getTranscriptText()).startsWith("We're no strangers");

    Optional<Video> storedVideo = videoRepository.findByYoutubeId(videoId);
    assertThat(storedVideo).isPresent();
    assertThat(storedVideo.get().getTitle()).isEqualTo("Rick Astley - Never Gonna Give You Up");
  }

  @Test
  @DisplayName("Should NOT call Transcript Ingestor if video exists in Postgres (Cache HIT)")
  void shouldReturnFromDbOnCacheHit() {
    String videoId = "existing123";
    Video existing = Video.create(videoId, "Existing Video", "Test Channel");
    VideoContent content = VideoContent.create("Dummy transcript for testing cache hit");
    existing.setContent(content);
    videoRepository.save(existing);

    Video result = ingestionAdapter.getOrIngestVideo(YoutubeUrl.of("https://youtu.be/" + videoId));

    assertThat(result.getYoutubeId()).isEqualTo(videoId);

    verify(0, postRequestedFor(anyUrl()));
  }
}
