package io.tubeguardian.policyengine.domain.services;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.policyengine.domain.exception.AIProviderException;
import io.tubeguardian.policyengine.domain.exception.PolicyOutputParsingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Responsibility: Transform raw transcript text into a structured RiskProfile using RAG
 * (Retrieval-Augmented Generation) and LLM.
 */
@Service
public class GarmAnalysisService {

  private static final Logger log = LoggerFactory.getLogger(GarmAnalysisService.class);
  private static final int RETRIEVAL_TOP_K = 5;

  private final VectorStore vectorStore;
  private final ChatModel chatModel;

  @Value("classpath:prompts/garm-prompt.st")
  private Resource systemPromptResource;

  public GarmAnalysisService(VectorStore vectorStore, ChatModel chatModel) {
    this.vectorStore = vectorStore;
    this.chatModel = chatModel;
  }

  public AnalysisResult.RiskProfile analyzeTranscript(String transcript) {
    try {
      String policyContext = retrieveRelevantPolicies(transcript);
      return generateRiskProfile(transcript, policyContext);
    } catch (PolicyOutputParsingException e) {
      log.error("AI returned invalid JSON format.");
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during AI interaction", e);
      throw new AIProviderException("Failed to communicate with AI Provider", e);
    }
  }

  private String retrieveRelevantPolicies(String transcript) {
    SearchRequest request = SearchRequest.builder().query(transcript).topK(RETRIEVAL_TOP_K).build();

    List<Document> documents = vectorStore.similaritySearch(request);

    if (documents.isEmpty()) {
      log.warn("No relevant policies found in Vector Store.");
      return "No specific policies retrieved.";
    }

    return documents.stream()
        .map(Document::getFormattedContent)
        .collect(Collectors.joining("\n---\n"));
  }

  private AnalysisResult.RiskProfile generateRiskProfile(String transcript, String policyContext) {
    var outputConverter = new BeanOutputConverter<>(AnalysisResult.RiskProfile.class);

    PromptTemplate promptTemplate = new PromptTemplate(systemPromptResource);
    var prompt =
        promptTemplate.create(
            Map.of(
                "policies", policyContext,
                "transcript", transcript,
                "format", outputConverter.getFormat()));

    String content = null;

    try {
      var response = chatModel.call(prompt);
      content = response.getResult().getOutput().getText();
      if (content == null || content.isBlank()) {
        throw new PolicyOutputParsingException("AI returned null or empty content string", null);
      }
      return outputConverter.convert(content);
    } catch (Exception e) {
      throw new PolicyOutputParsingException(content, e);
    }
  }
}
