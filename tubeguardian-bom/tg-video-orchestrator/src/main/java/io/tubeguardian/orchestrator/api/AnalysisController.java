package io.tubeguardian.orchestrator.api;

import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.api.dto.BrandSuitabilityDto;
import io.tubeguardian.orchestrator.domain.AnalysisOrchestrationService;
import io.tubeguardian.orchestrator.domain.services.BrandSuitabilityService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

  private final AnalysisOrchestrationService analysisOrchestrationService;
  private final BrandSuitabilityService suitabilityService;

  public AnalysisController(
      AnalysisOrchestrationService analysisOrchestrationService,
      BrandSuitabilityService suitabilityService) {
    this.analysisOrchestrationService = analysisOrchestrationService;
    this.suitabilityService = suitabilityService;
  }

  @PostMapping
  public ResponseEntity<AnalysisJobResponse> startAnalysis(
      @Valid @RequestBody AnalysisRequest request) {
    AnalysisJobResponse response = analysisOrchestrationService.analyzeVideo(request);

    if (JobStatus.COMPLETED.name().equals(response.status())) {
      return ResponseEntity.ok(response);
    }

    URI location = URI.create("/api/v1/jobs/" + response.jobId());
    return ResponseEntity.accepted().location(location).body(response);
  }

  @GetMapping("/videos/{videoId}/suitability")
  public ResponseEntity<BrandSuitabilityDto> getBrandSuitability(
      @PathVariable UUID videoId, @RequestParam("brand_profile_id") String brandProfileId) {

    BrandSuitabilityDto report = suitabilityService.checkSuitability(videoId, brandProfileId);
    return ResponseEntity.ok(report);
  }
}
