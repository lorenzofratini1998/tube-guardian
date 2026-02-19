package io.tubeguardian.policyengine.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import tools.jackson.databind.ObjectMapper;

class DocumentFactoryTest {

  private final DocumentFactory factory = new DocumentFactory(new ObjectMapper());

  @Test
  void create_ShouldGenerateCorrectSemanticStringAndMetadata() {
    GarmPolicy policy =
        new GarmPolicy(
            "GARM-TEST",
            "Violence",
            "Content depicting harm.",
            Map.of("HIGH", "Real gore"),
            new GarmPolicy.ContextVectors(List.of("blood", "kill"), List.of("movies", "news")));

    Document document = factory.create(policy);

    assertThat(document.getFormattedContent())
        .contains("Policy Category: Violence")
        .contains("Definition: Content depicting harm")
        .contains("Unsafe Signals: blood, kill")
        .contains("Safe Exceptions (Contexts where this is allowed): movies, news");

    assertThat(document.getMetadata())
        .containsEntry("policy_id", "GARM-TEST")
        .containsEntry("category", "Violence")
        .containsKey("json_payload");
  }
}
