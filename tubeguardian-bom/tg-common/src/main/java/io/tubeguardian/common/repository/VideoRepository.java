package io.tubeguardian.common.repository;

import io.tubeguardian.common.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    Optional<Video> findByYoutubeId(String youtubeId);
}
