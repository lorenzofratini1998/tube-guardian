package io.tubeguardian.orchestrator.domain.services;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.BrandProfile;
import io.tubeguardian.common.domain.risk.RiskLevel;
import io.tubeguardian.common.exception.VideoNotFoundException;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.orchestrator.api.dto.BrandSuitabilityDto;
import io.tubeguardian.orchestrator.api.exception.BrandNotFoundException;
import io.tubeguardian.orchestrator.repository.BrandProfileRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandSuitabilityService {

  private final BrandProfileRepository brandProfileRepository;
  private final AnalysisResultRepository analysisResultRepository;

  public BrandSuitabilityService(
      BrandProfileRepository brandProfileRepository,
      AnalysisResultRepository analysisResultRepository) {
    this.brandProfileRepository = brandProfileRepository;
    this.analysisResultRepository = analysisResultRepository;
  }

  @Transactional(readOnly = true)
  public BrandSuitabilityDto checkSuitability(UUID videoId, String brandProfileId) {
    BrandProfile brand =
        brandProfileRepository
            .findById(brandProfileId)
            .orElseThrow(() -> new BrandNotFoundException(brandProfileId));

    AnalysisResult result =
        analysisResultRepository
            .findByVideoId(videoId)
            .orElseThrow(() -> new VideoNotFoundException(videoId));

    AnalysisResult.RiskProfile aiOutput = result.getFullResponse();
    List<BrandSuitabilityDto.Violation> violations = new ArrayList<>();

    for (AnalysisResult.CategoryDetail detectedCategory : aiOutput.categories()) {
      String categoryId = detectedCategory.policyId();
      RiskLevel detectedLevel = detectedCategory.riskLevel();

      String brandCategoryTolerance = brand.getTolerances().getOrDefault(categoryId, "LOW");
      RiskLevel maxTolerance = RiskLevel.valueOf(brandCategoryTolerance);

      if (isRiskHigher(detectedLevel, maxTolerance)) {
        violations.add(
            new BrandSuitabilityDto.Violation(
                categoryId,
                detectedLevel,
                maxTolerance,
                String.join("; ", detectedCategory.reasoning())));
      }
    }

    boolean isSuitable = violations.isEmpty();

    return new BrandSuitabilityDto(
        brand.getProfileId(), brand.getDisplayName(), isSuitable, violations);
  }

  private boolean isRiskHigher(RiskLevel detected, RiskLevel tolerance) {
    return detected.ordinal() > tolerance.ordinal();
  }
}
