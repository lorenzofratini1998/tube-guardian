package io.tubeguardian.orchestrator.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.AnalysisResult;
import io.tubeguardian.common.exception.ServiceUnavailableException;
import io.tubeguardian.orchestrator.api.dto.AnalysisJobResponse;
import io.tubeguardian.orchestrator.api.dto.AnalysisRequest;
import io.tubeguardian.orchestrator.api.exception.GlobalExceptionHandler;
import io.tubeguardian.orchestrator.domain.AnalysisOrchestrationService;
import io.tubeguardian.orchestrator.util.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalysisController.class)
@Import({GlobalExceptionHandler.class})
@AutoConfigureJsonTesters
class AnalysisControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JacksonTester<AnalysisRequest> jsonRequest;

  @MockitoBean private AnalysisOrchestrationService orchestrationService;

  @Test
  @DisplayName("POST /analyze - Should return 202 Accepted and Location header for new job")
  void shouldReturnAcceptedForNewJob() throws Exception {
    AnalysisRequest request = TestFixtures.validRequest();
    AnalysisJob job = TestFixtures.pendingJob(TestFixtures.videoEntity());
    AnalysisJobResponse response = AnalysisJobResponse.fromAnalysisJob(job);

    when(orchestrationService.analyzeVideo(any(AnalysisRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest.write(request).getJson()))
        .andExpect(status().isAccepted())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("POST /analyze - Should return 200 OK for Cached Result")
  void shouldReturnOkForCachedResult() throws Exception {
    AnalysisRequest request = TestFixtures.validRequest();
    AnalysisResult result = TestFixtures.analysisResult(TestFixtures.videoEntity());
    AnalysisJobResponse response = AnalysisJobResponse.fromAnalysisResult(result);

    when(orchestrationService.analyzeVideo(any(AnalysisRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest.write(request).getJson()))
        .andExpect(status().isOk())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.result").exists());
  }

  @Test
  @DisplayName("POST /analyze - Should return 400 Bad Request for invalid URL")
  void shouldReturnBadRequestForInvalidUrl() throws Exception {
    AnalysisRequest invalidRequest = new AnalysisRequest("not-a-url");

    mockMvc
        .perform(
            post("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest.write(invalidRequest).getJson()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Error"));
  }

  @Test
  @DisplayName("POST /analyze - Should return 503 when dependency fails")
  void shouldReturnServiceUnavailable() throws Exception {
    when(orchestrationService.analyzeVideo(any()))
        .thenThrow(new ServiceUnavailableException("ingestion-service"));

    mockMvc
        .perform(
            post("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest.write(TestFixtures.validRequest()).getJson()))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("Service Unavailable"));
  }
}
