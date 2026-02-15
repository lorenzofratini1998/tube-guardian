package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.risk.RiskLevel;

public record CategoryDetailDto(RiskLevel risk, String reasoning) {
  public static CategoryDetailDto fromDomain(AnalysisResult.CategoryDetail categoryDetail) {
    return new CategoryDetailDto(categoryDetail.riskLevel(), categoryDetail.reasoning());
  }
}
