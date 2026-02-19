package io.tubeguardian.orchestrator;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

  @Container
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management-alpine");

  static WireMockServer wireMockServer =
      new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static {
    wireMockServer.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("app.services.ingestion.url", () -> "http://localhost:" + wireMockServer.port());
    registry.add("spring.rabbitmq.host", rabbitmq::getHost);
    registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
  }

  @BeforeAll
  static void setupWireMockGlobal() {
    WireMock.configureFor(wireMockServer.port());
  }

  @AfterAll
  static void tearDownGlobal() {}

  @BeforeEach
  void resetWireMock() {
    wireMockServer.resetAll();
  }

  protected static void stubIngestionResponse(String videoId) {
    String url = "https://www.youtube.com/watch?v=" + videoId;

    stubFor(
        post(urlEqualTo("/api/v1/transcript"))
            .withRequestBody(matchingJsonPath("$.url", equalTo(url)))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                            {
                                "metadata": {
                                    "video_id": "%s",
                                    "title": "Rick Astley - Never Gonna Give You Up",
                                    "channel": "RickAstley",
                                    "video_url": "%s",
                                    "thumbnail_url": "http://img.youtube.com/vi/%s/0.jpg",
                                    "duration_seconds": 212,
                                    "upload_date": "20091025"
                                },
                                "transcript": {
                                    "text": "We're no strangers to love...",
                                    "language_code": "en",
                                    "is_auto_generated": false
                                },
                                "status": "success"
                            }
                        """
                            .formatted(videoId, url, videoId))
                    .withStatus(200)));
  }
}
