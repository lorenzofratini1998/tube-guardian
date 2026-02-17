package io.tubeguardian.orchestrator.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.event.AnalysisJobEvent;
import io.tubeguardian.common.domain.event.AnalysisResultEvent;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import io.tubeguardian.common.repository.VideoContentRepository;
import io.tubeguardian.orchestrator.AbstractIntegrationTest;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.domain.AnalysisOrchestrationService;
import io.tubeguardian.orchestrator.repository.AnalysisJobRepository;
import io.tubeguardian.common.repository.VideoRepository;
import io.tubeguardian.orchestrator.util.TestFixtures;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class AnalysisWorkflowIT extends AbstractIntegrationTest {
  @Autowired private AnalysisOrchestrationService orchestrationService;
  @Autowired private AnalysisJobRepository jobRepository;
  @Autowired private VideoRepository videoRepository;
  @Autowired private VideoContentRepository videoContentRepository;
  @Autowired private RabbitTemplate rabbitTemplate;

  @BeforeEach
  void cleanup() {
    jobRepository.deleteAllInBatch();
    videoContentRepository.deleteAllInBatch();
    videoRepository.deleteAllInBatch();

    rabbitTemplate.execute(
        channel -> {
          channel.queuePurge(RabbitMQConstants.QUEUE_ANALYSIS_JOBS);
          channel.queuePurge(RabbitMQConstants.QUEUE_ANALYSIS_RESULTS);
          return null;
        });
  }

  @Test
  @DisplayName("Producer Flow: analyzeVideo() should persist Job and push to RabbitMQ")
  void testProducerFlow() {
    AnalysisRequest request = TestFixtures.validRequest();
    stubIngestionResponse(TestFixtures.VIDEO_ID);

    orchestrationService.analyzeVideo(request);

    Video video = videoRepository.findByYoutubeId(TestFixtures.VIDEO_ID).orElseThrow();
    AnalysisJob job =
        jobRepository.findTopByVideoIdOrderByCreatedAtDesc(video.getId()).orElseThrow();
    assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);

    Object message = rabbitTemplate.receiveAndConvert(RabbitMQConstants.QUEUE_ANALYSIS_JOBS, 2000);

    assertThat(message).isNotNull();
    assertThat(message).isInstanceOf(AnalysisJobEvent.class);

    AnalysisJobEvent event = (AnalysisJobEvent) message;
    assertThat(event.videoId()).isEqualTo(video.getId());
    assertThat(event.jobId()).isEqualTo(job.getId());
    assertThat(event.youtubeId()).isEqualTo(video.getYoutubeId());
  }

  @Test
  @DisplayName("Consumer Flow: RabbitMQ Result message should mark Job as COMPLETED")
  void testConsumerFlow() {
    Video video = videoRepository.save(TestFixtures.videoEntity());
    AnalysisJob job = AnalysisJob.create(video);
    job.setStatus(JobStatus.PROCESSING);
    jobRepository.save(job);

    var resultEvent = new AnalysisResultEvent(video.getId(), JobStatus.COMPLETED);

    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE_VIDEO,
        RabbitMQConstants.ROUTING_KEY_RESULT_COMPLETED,
        resultEvent);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              AnalysisJob updatedJob = jobRepository.findById(job.getId()).orElseThrow();
              assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
            });
  }
}
