package io.tubeguardian.orchestrator.domain.services;

import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.VideoContent;
import io.tubeguardian.orchestrator.repository.VideoRepository;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.dto.VideoResponseDto;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VideoPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(VideoPersistenceService.class);
    private final VideoRepository videoRepository;

    public VideoPersistenceService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Transactional
    public Video saveVideoData(VideoResponseDto data) {
        var metadata = data.metadata();
        var transcript = data.transcript();

        Video video = Video.create(metadata.videoId(), metadata.title(), metadata.channel());
        video.setWebpageUrl(metadata.videoUrl());
        video.setDurationSeconds(metadata.durationSeconds());
        video.setThumbnailUrl(metadata.thumbnailUrl());

        if (metadata.uploadDate() != null) {
            try {
                video.setUploadDate(LocalDate.parse(metadata.uploadDate(), DateTimeFormatter.BASIC_ISO_DATE));
            } catch (Exception e) {
                log.warn("Failed to parse upload date: {}", metadata.uploadDate());
            }
        }

        VideoContent content = VideoContent.create(transcript.text());
        video.setContent(content);

        return videoRepository.save(video);
    }
}
