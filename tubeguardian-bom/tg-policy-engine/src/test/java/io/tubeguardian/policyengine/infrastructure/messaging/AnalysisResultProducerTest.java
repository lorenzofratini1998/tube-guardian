package io.tubeguardian.policyengine.infrastructure.messaging;

import static org.mockito.Mockito.verify;

import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import io.tubeguardian.policyengine.infrastructure.messaging.producer.AnalysisResultProducer;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class AnalysisResultProducerTest {

  @Mock private RabbitTemplate rabbitTemplate;

  @InjectMocks private AnalysisResultProducer producer;

  @Test
  @DisplayName("Should publish completion event to correct exchange and routing key")
  void shouldPublishCompletionEvent() {
    UUID videoId = UUID.randomUUID();

    AnalysisResultEvent event = new AnalysisResultEvent(videoId, JobStatus.COMPLETED);

    producer.sendCompletion(event);

    verify(rabbitTemplate)
        .convertAndSend(
            RabbitMQConstants.EXCHANGE_VIDEO,
            RabbitMQConstants.ROUTING_KEY_RESULT_COMPLETED,
            event);
  }
}
