package io.tubeguardian.orchestrator.api;

import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.domain.AnalysisOrchestrationService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analyze")
public class AnalysisController {

    private final AnalysisOrchestrationService analysisOrchestrationService;

    public AnalysisController(AnalysisOrchestrationService analysisOrchestrationService) {
        this.analysisOrchestrationService = analysisOrchestrationService;
    }

    @PostMapping
    public ResponseEntity<AnalysisJobResponse> startAnalysis(@Valid @RequestBody AnalysisRequest request) {
        AnalysisJobResponse response = analysisOrchestrationService.analyzeVideo(request);

        if (JobStatus.COMPLETED.name().equals(response.status())) {
            return ResponseEntity.ok(response);
        }

        URI location = URI.create("/api/v1/jobs/" + response.jobId());
        return ResponseEntity
                .accepted()
                .location(location)
                .body(response);
    }
}
