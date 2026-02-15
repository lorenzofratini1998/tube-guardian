package io.tubeguardian.common.repository;

import io.tubeguardian.common.domain.AnalysisJob;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, UUID> {
  Optional<AnalysisJob> findTopByVideoIdOrderByCreatedAtDesc(UUID videoId);
}
