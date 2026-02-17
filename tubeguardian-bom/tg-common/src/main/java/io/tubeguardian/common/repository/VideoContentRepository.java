package io.tubeguardian.common.repository;

import io.tubeguardian.common.domain.VideoContent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoContentRepository extends JpaRepository<VideoContent, UUID> {}
