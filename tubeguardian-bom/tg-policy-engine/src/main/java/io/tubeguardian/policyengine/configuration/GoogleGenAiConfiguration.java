package io.tubeguardian.policyengine.configuration;

import com.google.genai.Client;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
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

  @Value("${spring.ai.google.genai.model:gemini-embedding-001}")
  private String model;

  @Value("${spring.ai.google.genai.chat-model:gemini-1.5-flash}")
  private String chatModelName;

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

  @Bean
  public ChatModel chatModel() {
    Client client = Client.builder().apiKey(apiKey).build();

    GoogleGenAiChatOptions options =
        GoogleGenAiChatOptions.builder().model(chatModelName).temperature(0.0).build();

    return GoogleGenAiChatModel.builder().genAiClient(client).defaultOptions(options).build();
  }
}
