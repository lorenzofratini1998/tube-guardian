package io.tubeguardian.policyengine.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.VideoContent;
import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.domain.risk.RiskLevel;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.common.repository.VideoContentRepository;
import io.tubeguardian.common.repository.VideoRepository;
import io.tubeguardian.policyengine.AbstractIntegrationTest;
import io.tubeguardian.policyengine.domain.services.GarmAnalysisService;
import io.tubeguardian.policyengine.domain.services.PolicyEngineService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class PolicyWorkflowIT extends AbstractIntegrationTest {

  @Autowired private PolicyEngineService policyEngineService;
  @Autowired private VideoRepository videoRepository;
  @Autowired private VideoContentRepository videoContentRepository;
  @Autowired private AnalysisResultRepository analysisResultRepository;
  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private AmqpAdmin amqpAdmin;

  @MockitoBean private GarmAnalysisService garmAnalysisService;
  @MockitoBean private VectorStore vectorStore;

  @Test
  @DisplayName("End-to-End: Process Video -> Mock AI -> Save DB -> Publish RabbitMQ")
  void testFullAnalysisWorkflow() {
    Video video = Video.create("yt_id_123", "Test Title", "Test Channel");
    VideoContent content = VideoContent.create("Integration Test Transcript");
    video.setContent(content);
    videoRepository.save(video);
    UUID videoId = video.getId();

    AnalysisResult.RiskProfile mockProfile =
        new AnalysisResult.RiskProfile(RiskLevel.LOW, 100, "Summary", null);
    when(garmAnalysisService.analyzeTranscript(anyString())).thenReturn(mockProfile);

    String spyQueueName = "test.spy.results." + UUID.randomUUID();
    Queue spyQueue = new Queue(spyQueueName, false, false, true);
    TopicExchange exchange = new TopicExchange(RabbitMQConstants.EXCHANGE_VIDEO);

    amqpAdmin.declareQueue(spyQueue);
    amqpAdmin.declareExchange(exchange);
    amqpAdmin.declareBinding(
        BindingBuilder.bind(spyQueue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_KEY_RESULT_COMPLETED));

    policyEngineService.processVideo(videoId);

    Object received = rabbitTemplate.receiveAndConvert(spyQueueName, 5000);

    assertThat(received).isNotNull().isInstanceOf(AnalysisResultEvent.class);

    AnalysisResultEvent event = (AnalysisResultEvent) received;
    assertThat(event.videoId()).isEqualTo(videoId);
    assertThat(event.status()).isEqualTo(JobStatus.COMPLETED);

    List<AnalysisResult> results = analysisResultRepository.findAll();

    assertThat(results).isNotEmpty();
    AnalysisResult savedResult =
        results.stream()
            .filter(r -> r.getVideo().getId().equals(videoId))
            .findFirst()
            .orElseThrow();

    assertThat(savedResult.getFullResponse()).isNotNull();
    assertThat(savedResult.getFullResponse().safetyScore()).isEqualTo(100);
    assertThat(savedResult.getFullResponse().overallRiskLevel()).isEqualTo(RiskLevel.LOW);

    amqpAdmin.deleteQueue(spyQueueName);
  }
}
