package io.tubeguardian.policyengine.configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleGenAiConfiguration {

  @Value("${spring.ai.google.genai.api-key}")
  private String apiKey;

  @Value("${spring.ai.google.genai.model}")
  private String model;

  @Bean
  public EmbeddingModel embeddingModel() {
    GoogleGenAiEmbeddingConnectionDetails connectionDetails =
        GoogleGenAiEmbeddingConnectionDetails.builder().apiKey(apiKey).build();

    GoogleGenAiTextEmbeddingOptions options =
        GoogleGenAiTextEmbeddingOptions.builder()
            .model(model)
            .taskType(GoogleGenAiTextEmbeddingOptions.TaskType.RETRIEVAL_DOCUMENT)
            .build();

    return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
  }
}
