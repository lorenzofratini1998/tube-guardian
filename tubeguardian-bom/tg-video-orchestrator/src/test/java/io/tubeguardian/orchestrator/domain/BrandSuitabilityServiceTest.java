package io.tubeguardian.orchestrator.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.domain.AnalysisResult.RiskProfile;
import io.tubeguardian.common.domain.BrandProfile;
import io.tubeguardian.common.domain.risk.RiskLevel;
import io.tubeguardian.common.repository.AnalysisResultRepository;
import io.tubeguardian.orchestrator.api.dto.BrandSuitabilityDto;
import io.tubeguardian.orchestrator.api.exception.BrandNotFoundException;
import io.tubeguardian.orchestrator.domain.services.BrandSuitabilityService;
import io.tubeguardian.orchestrator.repository.BrandProfileRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandSuitabilityServiceTest {

  @Mock private BrandProfileRepository brandProfileRepository;
  @Mock private AnalysisResultRepository analysisResultRepository;

  @InjectMocks private BrandSuitabilityService service;

  @Test
  @DisplayName("Should return SUITABLE when risk is within tolerance")
  void checkSuitability_Suitable() {
    UUID videoId = UUID.randomUUID();
    String brandId = "REDBULL";

    BrandProfile brand = mock(BrandProfile.class);
    when(brand.getProfileId()).thenReturn(brandId);
    when(brand.getTolerances()).thenReturn(Map.of("GARM-ARMS", "HIGH"));
    when(brandProfileRepository.findById(brandId)).thenReturn(Optional.of(brand));

    AnalysisResult result = mockAnalysisResult(videoId, "GARM-ARMS");
    when(analysisResultRepository.findByVideoId(videoId)).thenReturn(Optional.of(result));

    BrandSuitabilityDto dto = service.checkSuitability(videoId, brandId);

    assertThat(dto.isSuitable()).isTrue();
    assertThat(dto.violations()).isEmpty();
  }

  @Test
  @DisplayName("Should return UNSUITABLE when risk exceeds tolerance")
  void checkSuitability_Unsuitable() {
    UUID videoId = UUID.randomUUID();
    String brandId = "DISNEY";

    BrandProfile brand = mock(BrandProfile.class);
    when(brand.getProfileId()).thenReturn(brandId);
    when(brand.getTolerances()).thenReturn(Map.of("GARM-OBSCENE", "LOW"));
    when(brandProfileRepository.findById(brandId)).thenReturn(Optional.of(brand));

    AnalysisResult result = mockAnalysisResult(videoId, "GARM-OBSCENE");
    when(analysisResultRepository.findByVideoId(videoId)).thenReturn(Optional.of(result));

    BrandSuitabilityDto dto = service.checkSuitability(videoId, brandId);

    assertThat(dto.isSuitable()).isFalse();
    assertThat(dto.violations()).hasSize(1);

    BrandSuitabilityDto.Violation violation = dto.violations().get(0);
    assertThat(violation.categoryId()).isEqualTo("GARM-OBSCENE");
    assertThat(violation.riskLevel()).isEqualTo(RiskLevel.MEDIUM);
    assertThat(violation.maxTolerance()).isEqualTo(RiskLevel.LOW);
  }

  @Test
  @DisplayName("Should apply DEFAULT LOW tolerance when brand rule is missing")
  void checkSuitability_DefaultTolerance() {
    // ARRANGE
    UUID videoId = UUID.randomUUID();
    String brandId = "STRICT_BRAND";

    BrandProfile brand = mock(BrandProfile.class);
    when(brand.getProfileId()).thenReturn(brandId);
    when(brand.getTolerances()).thenReturn(Collections.emptyMap());
    when(brandProfileRepository.findById(brandId)).thenReturn(Optional.of(brand));

    AnalysisResult result = mockAnalysisResult(videoId, "GARM-SOCIAL");
    when(analysisResultRepository.findByVideoId(videoId)).thenReturn(Optional.of(result));

    BrandSuitabilityDto dto = service.checkSuitability(videoId, brandId);

    assertThat(dto.isSuitable()).isFalse();
    assertThat(dto.violations().get(0).maxTolerance()).isEqualTo(RiskLevel.LOW);
  }

  @Test
  @DisplayName("Should throw exception if Brand or Video not found")
  void checkSuitability_NotFound() {
    UUID videoId = UUID.randomUUID();
    when(brandProfileRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.checkSuitability(videoId, "UNKNOWN"))
        .isInstanceOf(BrandNotFoundException.class);
  }

  private AnalysisResult mockAnalysisResult(UUID videoId, String policyId) {
    AnalysisResult result = mock(AnalysisResult.class);

    AnalysisResult.CategoryDetail category =
        new AnalysisResult.CategoryDetail(policyId, RiskLevel.MEDIUM, List.of("Bad word detected"));

    RiskProfile profile = new RiskProfile(RiskLevel.MEDIUM, 50, "Summary", List.of(category));

    when(result.getFullResponse()).thenReturn(profile);
    return result;
  }
}
