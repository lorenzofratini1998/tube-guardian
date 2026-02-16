package io.tubeguardian.policyengine.domain.repository;

import io.tubeguardian.policyengine.domain.entities.PolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, String> {}
