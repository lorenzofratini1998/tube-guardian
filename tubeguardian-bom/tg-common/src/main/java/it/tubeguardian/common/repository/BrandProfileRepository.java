package it.tubeguardian.common.repository;

import it.tubeguardian.common.domain.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandProfileRepository extends JpaRepository<BrandProfile, String> {
}
