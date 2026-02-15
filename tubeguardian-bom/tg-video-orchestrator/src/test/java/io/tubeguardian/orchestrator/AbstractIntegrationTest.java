package io.tubeguardian.orchestrator;

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
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

  static WireMockServer wireMockServer =
      new WireMockServer(WireMockConfiguration.options().dynamicPort());

  static {
    postgres.start();
    wireMockServer.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("app.services.ingestion.url", () -> "http://localhost:" + wireMockServer.port());
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
}
