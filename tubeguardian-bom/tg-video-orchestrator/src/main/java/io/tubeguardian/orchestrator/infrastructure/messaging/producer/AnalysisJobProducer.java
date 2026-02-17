package io.tubeguardian.orchestrator.infrastructure.messaging.producer;

import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisJobProducer {

  private static final Logger log = LoggerFactory.getLogger(AnalysisJobProducer.class);
  private final RabbitTemplate rabbitTemplate;

  public AnalysisJobProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishJob(AnalysisJobEvent event) {
    log.info("Publishing analysis job for VideoID: {}", event.videoId());

    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE_VIDEO, RabbitMQConstants.ROUTING_KEY_JOB_CREATED, event);
  }
}
