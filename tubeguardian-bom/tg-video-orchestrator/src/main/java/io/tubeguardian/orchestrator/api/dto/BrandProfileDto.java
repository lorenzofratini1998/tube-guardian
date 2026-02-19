package io.tubeguardian.orchestrator.api.dto;

import io.tubeguardian.common.domain.BrandProfile;

public record BrandProfileDto(String id, String displayName, String logoUrl) {
  public static BrandProfileDto fromEntity(BrandProfile entity) {
    return new BrandProfileDto(entity.getProfileId(), entity.getDisplayName(), entity.getLogoUrl());
  }
}
