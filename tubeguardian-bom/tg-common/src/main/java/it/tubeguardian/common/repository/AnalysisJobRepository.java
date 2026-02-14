package it.tubeguardian.common.repository;

import it.tubeguardian.common.domain.AnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, UUID> {
    Optional<AnalysisJob> findTopByVideoIdOrderByCreatedAtDesc(UUID videoId);
}
