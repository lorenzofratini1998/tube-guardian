package io.tubeguardian.orchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record AnalysisRequest(
    @NotBlank(message = "URL is required") @URL(message = "Invalid URL format") String url) {}
