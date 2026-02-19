package io.tubeguardian.orchestrator.infrastructure;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.orchestrator.infrastructure.messaging.AnalysisJobEventDispatcher;
import io.tubeguardian.orchestrator.infrastructure.messaging.producer.AnalysisJobProducer;
import io.tubeguardian.orchestrator.util.TestFixtures;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisJobEventDispatcherTest {

  @Mock private AnalysisJobProducer producer;

  @InjectMocks private AnalysisJobEventDispatcher dispatcher;

  @Test
  @DisplayName("Scenario: Event Received -> Delegate to Producer")
  void shouldDelegateToProducer_WhenEventIsReceived() {
    AnalysisJobEvent mockEvent =
        new AnalysisJobEvent(UUID.randomUUID(), UUID.randomUUID(), TestFixtures.VIDEO_ID, "Title");

    dispatcher.onJobReady(mockEvent);

    verify(producer, times(1)).publishJob(mockEvent);
  }
}
