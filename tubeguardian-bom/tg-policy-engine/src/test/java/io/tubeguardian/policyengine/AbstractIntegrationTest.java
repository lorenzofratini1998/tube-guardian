package io.tubeguardian.policyengine;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(
    properties = {
      "app.garm.policy-file=classpath:test_policies.json",
      "spring.ai.google.genai.api-key=dummy-key",
      "spring.ai.google.genai.model=gemini-embedding-001",
      "spring.flyway.enabled=true"
    })
public abstract class AbstractIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

  @Container
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management-alpine");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.host", rabbitmq::getHost);
    registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
  }
}
