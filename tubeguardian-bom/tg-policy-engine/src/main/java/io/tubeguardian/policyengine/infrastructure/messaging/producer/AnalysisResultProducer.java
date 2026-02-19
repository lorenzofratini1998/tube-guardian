package io.tubeguardian.policyengine.infrastructure.messaging.producer;

import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultProducer {

  private static final Logger log = LoggerFactory.getLogger(AnalysisResultProducer.class);

  private final RabbitTemplate rabbitTemplate;

  public AnalysisResultProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void sendCompletion(AnalysisResultEvent event) {
    log.info("Publishing analysis result for VideoID: [{}]", event.videoId());

    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE_VIDEO, RabbitMQConstants.ROUTING_KEY_RESULT_COMPLETED, event);
  }
}
