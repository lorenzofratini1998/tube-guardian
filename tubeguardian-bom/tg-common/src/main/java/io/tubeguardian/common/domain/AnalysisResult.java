package io.tubeguardian.common.domain;

import io.tubeguardian.common.domain.risk.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "analysis_results")
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

    protected AnalysisResult() {
    }

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

    public record RiskProfile(
            RiskLevel overallRiskLevel,
            int safetyScore, // 0 - 100
            String analysisSummary,
            Map<String, CategoryDetail> categories
    ) {}

    public record CategoryDetail(
            RiskLevel riskLevel,
            String reasoning
    ) {}
}
