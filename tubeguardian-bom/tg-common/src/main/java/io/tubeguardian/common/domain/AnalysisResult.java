package io.tubeguardian.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.tubeguardian.common.domain.risk.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "analysis_results", schema = "analysis_domain")
public class AnalysisResult extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "video_id", nullable = false)
  private Video video;

  @Column(name = "model_used", length = 50)
  private String modelUsed = "gemini-pro";

  @Column(name = "prompt_version", length = 20)
  private String promptVersion = "v1.0";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "full_response", columnDefinition = "jsonb", nullable = false)
  private RiskProfile fullResponse;

  @Column(name = "analyzed_at")
  private LocalDateTime analyzedAt;

  protected AnalysisResult() {}

  public static AnalysisResult create(Video video, RiskProfile profile) {
    AnalysisResult res = new AnalysisResult();
    res.video = video;
    res.fullResponse = profile;
    res.analyzedAt = LocalDateTime.now();
    return res;
  }

  public Video getVideo() {
    return video;
  }

  public String getModelUsed() {
    return modelUsed;
  }

  public String getPromptVersion() {
    return promptVersion;
  }

  public RiskProfile getFullResponse() {
    return fullResponse;
  }

  public LocalDateTime getAnalyzedAt() {
    return analyzedAt;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RiskProfile(
      @JsonProperty("overall_risk") RiskLevel overallRiskLevel,
      @JsonProperty("safety_score") int safetyScore, // 0 - 100
      @JsonProperty("analysis_summary") String analysisSummary,
      @JsonProperty("categories") List<CategoryDetail> categories) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CategoryDetail(
      @JsonProperty("policy_id") String policyId,
      @JsonProperty("risk_level") RiskLevel riskLevel,
      @JsonProperty("reasoning") List<String> reasoning) {}
}
