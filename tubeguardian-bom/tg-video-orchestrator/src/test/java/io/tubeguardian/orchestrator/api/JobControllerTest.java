package io.tubeguardian.orchestrator.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.domain.AnalysisOrchestrationService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JobController.class)
class JobControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AnalysisOrchestrationService analysisOrchestrationService;

  @Test
  void getJobStatus_WhenJobExists_ShouldReturn200AndStatus() throws Exception {
    UUID jobId = UUID.randomUUID();
    UUID videoId = UUID.randomUUID();

    AnalysisJobResponse mockResponse =
        new AnalysisJobResponse(
            jobId, videoId, JobStatus.COMPLETED.name(), LocalDateTime.now(), null);

    when(analysisOrchestrationService.getJobStatus(jobId)).thenReturn(mockResponse);

    mockMvc
        .perform(get("/api/v1/jobs/{jobId}", jobId))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.jobId").value(jobId.toString()))
        .andExpect(jsonPath("$.videoId").value(videoId.toString()))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }
}
