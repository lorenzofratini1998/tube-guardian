package io.tubeguardian.orchestrator.api;

import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.domain.AnalysisOrchestrationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

  private final AnalysisOrchestrationService analysisOrchestrationService;

  public JobController(AnalysisOrchestrationService analysisOrchestrationService) {
    this.analysisOrchestrationService = analysisOrchestrationService;
  }

  @GetMapping("/{jobId}")
  public ResponseEntity<AnalysisJobResponse> getJobStatus(@PathVariable UUID jobId) {
    AnalysisJobResponse status = analysisOrchestrationService.getJobStatus(jobId);
    return ResponseEntity.ok(status);
  }
}
