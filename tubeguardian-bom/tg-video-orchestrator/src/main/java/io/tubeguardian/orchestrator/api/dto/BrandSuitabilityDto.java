package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.risk.RiskLevel;
import java.util.List;

public record BrandSuitabilityDto(
    String brandId, String brandName, boolean isSuitable, List<Violation> violations) {

  public record Violation(
      String categoryId, RiskLevel riskLevel, RiskLevel maxTolerance, String reasoning) {}
}
