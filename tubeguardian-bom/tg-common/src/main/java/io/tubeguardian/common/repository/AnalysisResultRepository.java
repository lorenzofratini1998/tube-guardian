package io.tubeguardian.common.repository;

import io.tubeguardian.common.domain.AnalysisResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {
    Optional<AnalysisResult> findByVideoId(UUID videoId);
}
