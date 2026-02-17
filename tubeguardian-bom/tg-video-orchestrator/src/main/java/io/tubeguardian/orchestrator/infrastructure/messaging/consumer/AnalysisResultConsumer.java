package io.tubeguardian.orchestrator.infrastructure.messaging.consumer;

import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import io.tubeguardian.orchestrator.domain.services.AnalysisJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisResultConsumer {

  private static final Logger log = LoggerFactory.getLogger(AnalysisResultConsumer.class);

  private final AnalysisJobService jobService;

  public AnalysisResultConsumer(AnalysisJobService jobService) {
    this.jobService = jobService;
  }

  @RabbitListener(queues = RabbitMQConstants.QUEUE_ANALYSIS_RESULTS)
  public void onAnalysisResult(AnalysisResultEvent event) {
    log.info("Received analysis result for VideoID: [{}]", event.videoId());
    jobService.markJobCompletion(event.videoId(), event.status());
  }
}
