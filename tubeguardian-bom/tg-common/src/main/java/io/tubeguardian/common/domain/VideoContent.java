package io.tubeguardian.common.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "video_contents")
public class VideoContent {

    @Id
    @Column(name = "video_id")
    private UUID videoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String transcriptText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected VideoContent() {}

    public static VideoContent create(String transcriptText) {
        VideoContent vc = new VideoContent();
        vc.transcriptText = transcriptText;
        return vc;
    }

    void setVideo(Video video) {
        this.video = video;
    }

    public VideoContent setTranscriptText(String text) {
        this.transcriptText = text;
        return this;
    }

    public UUID getVideoId() { return videoId; }
    public Video getVideo() { return video; }
    public String getTranscriptText() { return transcriptText; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoContent that)) return false;
        return videoId != null && Objects.equals(videoId, that.videoId);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
