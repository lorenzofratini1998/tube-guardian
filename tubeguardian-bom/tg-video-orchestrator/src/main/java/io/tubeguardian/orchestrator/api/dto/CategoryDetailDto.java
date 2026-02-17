package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.risk.RiskLevel;
import java.util.List;

public record CategoryDetailDto(String policyId, RiskLevel risk, List<String> reasoning) {
  public static CategoryDetailDto fromDomain(AnalysisResult.CategoryDetail categoryDetail) {
    return new CategoryDetailDto(
        categoryDetail.policyId(), categoryDetail.riskLevel(), categoryDetail.reasoning());
  }
}
