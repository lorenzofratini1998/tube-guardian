package io.tubeguardian.orchestrator.infrastructure.client.ingestion;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.tubeguardian.common.exception.ServiceUnavailableException;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.dto.VideoRequestDto;
import io.tubeguardian.orchestrator.infrastructure.client.ingestion.dto.VideoResponseDto;
import java.net.http.HttpClient;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VideoServiceClient {

  private static final Logger log = LoggerFactory.getLogger(VideoServiceClient.class);
  private static final String SERVICE_NAME = "ingestion-service";

  private final RestClient restClient;

  public VideoServiceClient(
      RestClient.Builder builder,
      @Value("${app.services.ingestion.url}") String url,
      @Value("${app.services.ingestion.timeout:30s}") Duration timeout) {
    HttpClient httpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(timeout)
            .build();

    var requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(timeout);

    this.restClient = builder.baseUrl(url).requestFactory(requestFactory).build();
  }

  @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "fallbackFetchVideoData")
  @Retry(name = SERVICE_NAME)
  public VideoResponseDto fetchVideoData(String url) {
    log.debug("Calling service {} for URL: {}", SERVICE_NAME, url);
    return restClient
        .post()
        .uri("/api/v1/transcript")
        .body(new VideoRequestDto(url))
        .retrieve()
        .body(VideoResponseDto.class);
  }

  protected VideoResponseDto fallbackFetchVideoData(String url, Throwable t) {
    log.error("Failed to fetch video data for URL: {}. Reason: {}", url, t.getMessage());
    throw new ServiceUnavailableException(SERVICE_NAME);
  }
}
