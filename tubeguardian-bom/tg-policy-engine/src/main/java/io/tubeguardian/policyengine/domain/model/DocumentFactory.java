package io.tubeguardian.policyengine.domain.model;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class DocumentFactory {

  private static final Logger log = LoggerFactory.getLogger(DocumentFactory.class);
  private final ObjectMapper objectMapper;

  public DocumentFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Document create(GarmPolicy policy) {
    String content = policy.toSematicString();
    Map<String, Object> metadata =
        Map.of(
            "policy_id", policy.id(),
            "category", policy.category(),
            "json_payload", toJson(policy));
    return new Document(content, metadata);
  }

  private String toJson(GarmPolicy policy) {
    try {
      return objectMapper.writeValueAsString(policy);
    } catch (JacksonException e) {
      log.error("Failed to serialize policy ID: {}", policy.id(), e);
      return "{}";
    }
  }
}
