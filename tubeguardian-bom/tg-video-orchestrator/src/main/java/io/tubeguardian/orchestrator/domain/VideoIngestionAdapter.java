package io.tubeguardian.orchestrator.domain;

import io.tubeguardian.common.domain.Video;
import io.tubeguardian.orchestrator.repository.VideoRepository;
import io.tubeguardian.orchestrator.domain.model.YoutubeUrl;
import io.tubeguardian.orchestrator.domain.services.VideoPersistenceService;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.VideoServiceClient;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.dto.VideoResponseDto;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VideoIngestionAdapter {

    private static final Logger log = LoggerFactory.getLogger(VideoIngestionAdapter.class);

    private final VideoRepository videoRepository;
    private final VideoServiceClient videoServiceClient;
    private final VideoPersistenceService videoPersistenceService;

    public VideoIngestionAdapter(VideoRepository videoRepository, VideoServiceClient videoServiceClient, VideoPersistenceService videoPersistenceService) {
        this.videoRepository = videoRepository;
        this.videoServiceClient = videoServiceClient;
        this.videoPersistenceService = videoPersistenceService;
    }

    public Video getOrIngestVideo(YoutubeUrl url) {
        Optional<Video> existingVideo = videoRepository.findByYoutubeId(url.videoId());

        if (existingVideo.isPresent()) {
            log.debug("Ingestion Cache HIT: Transcript for video [{}] already exists", url.videoId());
            return existingVideo.get();
        }

        log.info("Ingestion Cache MISS: Fetching data for video [{}] from external service", url.videoId());
        VideoResponseDto response = videoServiceClient.fetchVideoData(url.value());

        return videoPersistenceService.saveVideoData(response);
    }

}
