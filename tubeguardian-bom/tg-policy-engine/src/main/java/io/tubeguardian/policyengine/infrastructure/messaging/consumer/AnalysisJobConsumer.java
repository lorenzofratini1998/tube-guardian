package io.tubeguardian.policyengine.infrastructure.messaging.consumer;

import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import io.tubeguardian.policyengine.domain.services.PolicyEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalysisJobConsumer.class);

    private final PolicyEngineService engineService;

    public AnalysisJobConsumer(PolicyEngineService engineService) {
        this.engineService = engineService;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_ANALYSIS_JOBS)
    public void onJobReceived(AnalysisJobEvent event) {
        try {
            log.info("Received job for VideoID: [{}]", event.videoId());
            engineService.processVideo(event.videoId());
        } catch (Exception e) {
            log.error("Error processing video [{}]", event.videoId(), e);
            throw e;
        }
    }
}
