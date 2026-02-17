package io.tubeguardian.policyengine.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.tubeguardian.common.domain.AnalysisResult.RiskProfile;
import io.tubeguardian.policyengine.domain.exception.AIProviderException;
import io.tubeguardian.policyengine.domain.exception.PolicyOutputParsingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GarmAnalysisServiceTest {

  @Mock private VectorStore vectorStore;
  @Mock private ChatModel chatModel;

  @InjectMocks private GarmAnalysisService service;

  private static final String VALID_JSON_RESPONSE =
      """
        {
          "overall_risk": "LOW",
          "safety_score": 95,
          "analysis_summary": "The content is safe",
          "categories": []
        }
        """;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(
        service,
        "systemPromptResource",
        new ByteArrayResource(
            "Start Prompt. Context: {policies}. Transcript: {transcript}. Format: {format}"
                .getBytes()));
  }

  @Test
  @DisplayName("Should retrieve policies and parse valid JSON response")
  void shouldAnalyzeSuccessfully() {
    String transcript = "Some transcript";

    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(List.of(new Document("Policy Content 1")));

    AssistantMessage assistantMessage = new AssistantMessage(VALID_JSON_RESPONSE);
    Generation generation = new Generation(assistantMessage);
    ChatResponse chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    RiskProfile result = service.analyzeTranscript(transcript);

    assertThat(result).isNotNull();
    assertThat(result.overallRiskLevel().name()).isEqualTo("LOW");
    assertThat(result.safetyScore()).isEqualTo(95);
  }

  @Test
  @DisplayName("Should throw PolicyOutputParsingException when AI returns broken JSON")
  void shouldThrowOnInvalidJson() {
    when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

    AssistantMessage assistantMessage = new AssistantMessage("I am sorry, I cannot answer this.");
    Generation generation = new Generation(assistantMessage);
    ChatResponse chatResponse = new ChatResponse(List.of(generation));
    when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

    assertThatThrownBy(() -> service.analyzeTranscript("test"))
        .isInstanceOf(PolicyOutputParsingException.class);
  }

  @Test
  @DisplayName("Should wrap unexpected infrastructure errors in AIProviderException")
  void shouldWrapInfrastructureErrors() {
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
            .thenThrow(new RuntimeException("Qdrant connection refused"));

    // Act & Assert
    assertThatThrownBy(() -> service.analyzeTranscript("test transcript"))
            .isInstanceOf(AIProviderException.class)
            .hasMessageContaining("Failed to communicate with AI Provider")
            .hasRootCauseMessage("Qdrant connection refused");
  }
}
