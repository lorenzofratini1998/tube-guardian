package io.tubeguardian.policyengine.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import io.tubeguardian.policyengine.AbstractIntegrationTest;
import io.tubeguardian.policyengine.domain.services.PolicyEngineService;
import io.tubeguardian.policyengine.infrastructure.messaging.producer.AnalysisResultProducer;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class MessagingIntegrationTest extends AbstractIntegrationTest {

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private AnalysisResultProducer producer;
  @Autowired private ApplicationContext applicationContext;

  @MockitoBean private PolicyEngineService engineService;
  @MockitoBean private VectorStore vectorStore;

  @Test
  @DisplayName("Consumer Flow: Should consume message from Queue and call Service")
  void testConsumerFlow() {
    UUID videoId = UUID.randomUUID();
    AnalysisJobEvent event =
        new AnalysisJobEvent(UUID.randomUUID(), videoId, "Integration Test Video", "TEST_YT_ID");

    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE_VIDEO, RabbitMQConstants.ROUTING_KEY_JOB_CREATED, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              verify(engineService).processVideo(videoId);
            });
  }

  @Test
  @DisplayName("Producer Flow: Should send message to Exchange")
  void testProducerFlow() {
    String spyQueueName = "spy.results.queue";
    org.springframework.amqp.core.Queue spyQueue =
        new org.springframework.amqp.core.Queue(spyQueueName);

    org.springframework.amqp.core.AmqpAdmin admin =
        applicationContext.getBean(org.springframework.amqp.core.AmqpAdmin.class);

    admin.declareQueue(spyQueue);
    admin.declareBinding(
        new org.springframework.amqp.core.Binding(
            spyQueueName,
            org.springframework.amqp.core.Binding.DestinationType.QUEUE,
            RabbitMQConstants.EXCHANGE_VIDEO,
            RabbitMQConstants.ROUTING_KEY_RESULT_COMPLETED,
            null));

    UUID videoId = UUID.randomUUID();
    AnalysisResultEvent event = new AnalysisResultEvent(videoId, JobStatus.COMPLETED);

    producer.sendCompletion(event);

    Object received = rabbitTemplate.receiveAndConvert(spyQueueName, 2000);

    assertThat(received).isNotNull();
    assertThat(received).isInstanceOf(AnalysisResultEvent.class);
    AnalysisResultEvent result = (AnalysisResultEvent) received;
    assertThat(result.videoId()).isEqualTo(videoId);

    admin.deleteQueue(spyQueueName);
  }
}
