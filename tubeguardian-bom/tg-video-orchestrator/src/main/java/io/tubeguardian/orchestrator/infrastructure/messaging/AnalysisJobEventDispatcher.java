package io.tubeguardian.orchestrator.infrastructure.messaging;

import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.orchestrator.infrastructure.messaging.producer.AnalysisJobProducer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AnalysisJobEventDispatcher {
  private final AnalysisJobProducer producer;

  public AnalysisJobEventDispatcher(AnalysisJobProducer producer) {
    this.producer = producer;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onJobReady(AnalysisJobEvent event) {
    producer.publishJob(event);
  }
}
