package io.tubeguardian.common.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "videos",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_videos_youtube_id", columnNames = "youtube_id")
    })
public class Video extends BaseEntity {

  @Column(name = "youtube_id", nullable = false, length = 20)
  private String youtubeId;

  @Column(nullable = false)
  private String title = "Unknown";

  @Column(nullable = false)
  private String channel = "Unknown";

  @Column(name = "webpage_url")
  private String webpageUrl;

  @Column(name = "upload_date")
  private LocalDate uploadDate;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @OneToOne(mappedBy = "video", cascade = CascadeType.ALL, optional = false)
  private VideoContent content;

  protected Video() {}

  public static Video create(String videoId, String title, String channel) {
    Video video = new Video();
    video.youtubeId = videoId;
    video.title = title;
    video.channel = channel;
    return video;
  }

  public String getYoutubeId() {
    return youtubeId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getWebpageUrl() {
    return webpageUrl;
  }

  public void setWebpageUrl(String webpageUrl) {
    this.webpageUrl = webpageUrl;
  }

  public LocalDate getUploadDate() {
    return uploadDate;
  }

  public void setUploadDate(LocalDate uploadDate) {
    this.uploadDate = uploadDate;
  }

  public Integer getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(Integer durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public VideoContent getContent() {
    return content;
  }

  public void setContent(VideoContent content) {
    this.content = content;
    if (content != null) {
      content.setVideo(this);
    }
  }
}
