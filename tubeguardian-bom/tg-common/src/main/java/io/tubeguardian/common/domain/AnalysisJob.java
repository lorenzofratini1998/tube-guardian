package io.tubeguardian.common.domain;

import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.exception.InvalidJobStateTransitionException;
import jakarta.persistence.*;

@Entity
@Table(name = "analysis_jobs", indexes = {
        @Index(name = "idx_jobs_status", columnList = "status")
})
public class AnalysisJob extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected AnalysisJob() {}

    public static AnalysisJob create(Video video) {
        AnalysisJob job = new AnalysisJob();
        job.video = video;
        job.status = JobStatus.PENDING;
        return job;
    }

    public AnalysisJob setStatus(JobStatus newStatus) {
        if (this.status.isTerminal() && !newStatus.isTerminal()) {
            throw new InvalidJobStateTransitionException(this.getId(), this.status, newStatus);
        }
        this.status = newStatus;
        return this;
    }

    public AnalysisJob setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Video getVideo() {
        return video;
    }

    public JobStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
