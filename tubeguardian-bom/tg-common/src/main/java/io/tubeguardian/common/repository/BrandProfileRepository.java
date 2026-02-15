package io.tubeguardian.common.repository;

import io.tubeguardian.common.domain.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandProfileRepository extends JpaRepository<BrandProfile, String> {
}
