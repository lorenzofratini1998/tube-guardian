package io.tubeguardian.policyengine.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.policyengine.domain.services.PolicyEngineService;
import io.tubeguardian.policyengine.infrastructure.messaging.consumer.AnalysisJobConsumer;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisJobConsumerTest {

  @Mock private PolicyEngineService engineService;

  @InjectMocks private AnalysisJobConsumer consumer;

  @Test
  @DisplayName("Should deserialize event and delegate to service")
  void shouldDelegateToService() {
    UUID videoId = UUID.randomUUID();
    AnalysisJobEvent event =
        new AnalysisJobEvent(UUID.randomUUID(), videoId, "Rick Astley", "dQw4w9WgXcQ");

    consumer.onJobReceived(event);

    verify(engineService).processVideo(videoId);
  }

  @Test
  @DisplayName("Should propagate exception to trigger RabbitMQ retry/DLQ")
  void shouldPropagateException() {
    UUID videoId = UUID.randomUUID();
    AnalysisJobEvent event =
        new AnalysisJobEvent(UUID.randomUUID(), videoId, "Fail Video", "FAIL123");

    doThrow(new RuntimeException("AI Error")).when(engineService).processVideo(any());

    assertThatThrownBy(() -> consumer.onJobReceived(event))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("AI Error");
  }
}
