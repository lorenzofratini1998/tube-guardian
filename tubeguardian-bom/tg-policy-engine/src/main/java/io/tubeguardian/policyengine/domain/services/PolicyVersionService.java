package io.tubeguardian.policyengine.domain.services;

import io.tubeguardian.policyengine.domain.entities.PolicyVersion;
import io.tubeguardian.policyengine.domain.repository.PolicyVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyVersionService {

  private static final Logger log = LoggerFactory.getLogger(PolicyVersionService.class);
  private static final String POLICY_KEY = "GARM_MASTER_POLICY";

  private final PolicyVersionRepository repository;

  public PolicyVersionService(PolicyVersionRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public boolean isUpToDate(String currentHash) {
    return repository
        .findById(POLICY_KEY)
        .map(version -> version.getContentHash().equals(currentHash))
        .orElse(false);
  }

  @Transactional
  public void updateVersion(String newHash) {
    PolicyVersion version =
        repository.findById(POLICY_KEY).orElse(PolicyVersion.create(POLICY_KEY, newHash));

    version.setContentHash(newHash);
    repository.save(version);
    log.info("Policy Version updated in DB. New Hash: {}", newHash);
  }
}
