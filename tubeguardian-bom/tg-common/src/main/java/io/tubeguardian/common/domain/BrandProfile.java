package io.tubeguardian.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "brand_profiles", schema = "video_domain")
public class BrandProfile {

  @Id
  @Column(name = "profile_id", length = 50)
  private String profileId;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "logo_url", nullable = false)
  private String logoUrl = "https://placehold.co/200x100?text=Brand+Logo";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "tolerances", columnDefinition = "jsonb", nullable = false)
  private Map<String, String> tolerances;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  protected BrandProfile() {}

  public static BrandProfile create(
      String profileId, String displayName, String logoUrl, Map<String, String> tolerances) {
    BrandProfile bp = new BrandProfile();
    bp.profileId = profileId;
    bp.displayName = displayName;
    bp.logoUrl = logoUrl;
    bp.tolerances = tolerances;
    return bp;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public BrandProfile setTolerances(Map<String, String> tolerances) {
    this.tolerances = tolerances;
    return this;
  }

  public BrandProfile setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  public String getProfileId() {
    return profileId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public Map<String, String> getTolerances() {
    return tolerances;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BrandProfile that)) return false;
    return Objects.equals(profileId, that.profileId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
