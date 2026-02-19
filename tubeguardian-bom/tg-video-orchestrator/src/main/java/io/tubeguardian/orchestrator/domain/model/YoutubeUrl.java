package io.tubeguardian.orchestrator.domain.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record YoutubeUrl(String value, String videoId) {
  private static final Pattern YT_ID_PATTERN =
      Pattern.compile(
          "(?<=v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*");

  public YoutubeUrl {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("URL cannot be empty");
    }
    Matcher matcher = YT_ID_PATTERN.matcher(value);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid YouTube URL format: " + value);
    }
    videoId = matcher.group();
  }

  public static YoutubeUrl of(String url) {
    return new YoutubeUrl(url, null);
  }
}
