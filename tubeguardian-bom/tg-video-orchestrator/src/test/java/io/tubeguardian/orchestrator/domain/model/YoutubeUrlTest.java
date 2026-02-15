package io.tubeguardian.orchestrator.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class YoutubeUrlTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "http://www.youtube.com/v/dQw4w9WgXcQ"
    })
    @DisplayName("Should extract Video ID correctly from valid formats")
    void shouldExtractVideoId(String url) {
        YoutubeUrl youtubeUrl = YoutubeUrl.of(url);
        assertThat(youtubeUrl.videoId()).isEqualTo("dQw4w9WgXcQ");
        assertThat(youtubeUrl.value()).isEqualTo(url);
    }

    @Test
    @DisplayName("Should throw exception for null URL")
    void shouldThrowForNull() {
        assertThatThrownBy(() -> YoutubeUrl.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception for invalid format")
    void shouldThrowForInvalidFormat() {
        String invalidUrl = "https://google.com/not-youtube";
        assertThatThrownBy(() -> YoutubeUrl.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid YouTube URL format");
    }
}
