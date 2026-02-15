package io.tubeguardian.common.domain;

import io.tubeguardian.common.domain.AnalysisJob;
import io.tubeguardian.common.domain.Video;
import io.tubeguardian.common.domain.status.JobStatus;
import io.tubeguardian.common.exception.InvalidJobStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AnalysisJobTest {

    @Test
    @DisplayName("Should allow transition from PENDING to PROCESSING to COMPLETED")
    void testValidTransitions() {
        Video video = Video.create("v1", "t", "c");
        AnalysisJob job = AnalysisJob.create(video);

        assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);

        job.setStatus(JobStatus.PROCESSING);
        assertThat(job.getStatus()).isEqualTo(JobStatus.PROCESSING);

        job.setStatus(JobStatus.COMPLETED);
        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should throw exception when trying to move from terminal state to non-terminal")
    void testInvalidTransition() {
        Video video = Video.create("v1", "t", "c");
        AnalysisJob job = AnalysisJob.create(video);

        job.setStatus(JobStatus.FAILED);

        assertThatThrownBy(() -> job.setStatus(JobStatus.PROCESSING))
                .isInstanceOf(InvalidJobStateTransitionException.class)
                .hasMessageContaining("Invalid state transition");
    }
}
