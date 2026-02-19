package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.risk.RiskLevel;
import java.util.List;

public record RiskProfileDto(
    RiskLevel overallRiskLevel,
    int safetyScore,
    String analysisSummary,
    List<CategoryDetailDto> categories) {
  public static RiskProfileDto fromDomain(AnalysisResult.RiskProfile riskProfile) {
    if (riskProfile == null) return null;

    return new RiskProfileDto(
        riskProfile.overallRiskLevel(),
        riskProfile.safetyScore(),
        riskProfile.analysisSummary(),
        riskProfile.categories().stream().map(CategoryDetailDto::fromDomain).toList());
  }
}
