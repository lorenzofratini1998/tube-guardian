package io.tubeguardian.policyengine.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "policy_versions", schema = "analysis_domain")
public class PolicyVersion {

  @Id
  @Column(name = "policy_name")
  private String policyName;

  @Column(name = "content_hash", nullable = false)
  private String contentHash;

  @UpdateTimestamp
  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;

  protected PolicyVersion() {}

  public static PolicyVersion create(String policyName, String contentHash) {
    PolicyVersion pv = new PolicyVersion();
    pv.policyName = policyName;
    pv.contentHash = contentHash;
    return pv;
  }

  public void setContentHash(String contentHash) {
    this.contentHash = contentHash;
  }

  public String getPolicyName() {
    return policyName;
  }

  public String getContentHash() {
    return contentHash;
  }

  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }
}
