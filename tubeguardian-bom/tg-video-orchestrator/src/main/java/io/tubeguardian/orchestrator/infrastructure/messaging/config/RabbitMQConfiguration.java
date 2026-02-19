package io.tubeguardian.orchestrator.infrastructure.messaging.config;

import io.tubeguardian.common.infrastructure.mq.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

  @Bean
  public JacksonJsonMessageConverter converter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(converter());
    return template;
  }

  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(RabbitMQConstants.EXCHANGE_VIDEO);
  }

  @Bean
  public Queue jobsQueue() {
    return QueueBuilder.durable(RabbitMQConstants.QUEUE_ANALYSIS_JOBS)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", RabbitMQConstants.QUEUE_ANALYSIS_JOBS_DLQ)
        .build();
  }

  @Bean
  public Queue resultsQueue() {
    return QueueBuilder.durable(RabbitMQConstants.QUEUE_ANALYSIS_RESULTS).build();
  }

  @Bean
  public Binding bindingJobs() {
    return BindingBuilder.bind(jobsQueue())
        .to(exchange())
        .with(RabbitMQConstants.ROUTING_KEY_JOB_CREATED);
  }

  @Bean
  public Binding bindingResults() {
    return BindingBuilder.bind(resultsQueue())
        .to(exchange())
        .with(RabbitMQConstants.ROUTING_KEY_RESULT_COMPLETED);
  }
}
