package io.tubeguardian.common.domain.event;

import io.tubeguardian.common.domain.AnalysisJob;
import java.io.Serializable;
import java.util.UUID;

public record AnalysisJobEvent(UUID jobId, UUID videoId, String youtubeId, String title)
    implements Serializable {

  public static AnalysisJobEvent fromDomain(AnalysisJob job) {
    return new AnalysisJobEvent(
        job.getId(),
        job.getVideo().getId(),
        job.getVideo().getYoutubeId(),
        job.getVideo().getTitle());
  }
}
