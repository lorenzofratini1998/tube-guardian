package io.tubeguardian.policyengine.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record GarmPolicy(
    String id,
    String category,
    String definition,
    @JsonProperty("risk_levels") Map<String, String> riskLevels,
    @JsonProperty("context_vectors") ContextVectors contextVectors) {
  public record ContextVectors(
      @JsonProperty("unsafe_signals") List<String> unsafeSignals,
      @JsonProperty("safe_exceptions") List<String> safeExceptions) {}

  public String toSematicString() {
    return String.format(
        "Policy Category: %s. "
            + "Definition: %s. "
            + "High Risk Indicators: %s. "
            + "Unsafe Signals: %s. "
            + "Safe Exceptions (Contexts where this is allowed): %s.",
        this.category,
        this.definition,
        this.riskLevels.getOrDefault("HIGH", "N/A"),
        String.join(", ", this.contextVectors.unsafeSignals),
        String.join(", ", this.contextVectors.safeExceptions));
  }
}
