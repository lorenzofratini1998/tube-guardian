package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.risk.RiskLevel;
import java.util.Map;
import java.util.stream.Collectors;

public record RiskProfileDto(
        RiskLevel overallRiskLevel,
        int safetyScore,
        String analysisSummary,
        Map<String, CategoryDetailDto> categories
) {
    public static RiskProfileDto fromDomain(AnalysisResult.RiskProfile riskProfile) {
        if (riskProfile == null) return null;

        return new RiskProfileDto(
                riskProfile.overallRiskLevel(),
                riskProfile.safetyScore(),
                riskProfile.analysisSummary(),
                riskProfile.categories().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> CategoryDetailDto.fromDomain(entry.getValue())
                        ))
        );
    }
}
