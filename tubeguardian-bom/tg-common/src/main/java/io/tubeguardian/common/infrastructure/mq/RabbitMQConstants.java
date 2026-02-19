package io.tubeguardian.common.infrastructure.mq;

public final class RabbitMQConstants {

  // --- EXCHANGES ---
  public static final String EXCHANGE_VIDEO = "tg.video.exchange";

  // --- QUEUES ---
  public static final String QUEUE_ANALYSIS_JOBS = "video.analysis.jobs";
  public static final String QUEUE_ANALYSIS_RESULTS = "video.analysis.results";

  // DLQ (Dead Letter Queues)
  public static final String QUEUE_ANALYSIS_JOBS_DLQ = "video.analysis.jobs.dlq";

  // --- ROUTING KEYS ---
  public static final String ROUTING_KEY_JOB_CREATED = "job.created";
  public static final String ROUTING_KEY_RESULT_COMPLETED = "result.completed";

  private RabbitMQConstants() {}
}
