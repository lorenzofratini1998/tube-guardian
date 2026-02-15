package io.tubeguardian.common.domain.risk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RiskLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    UNKNOWN;

    private static final Logger log = LoggerFactory.getLogger(RiskLevel.class);

    public static RiskLevel fromString(String value) {
        if (value == null) return NONE;
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Received invalid RiskLevel from LLM: '{}'. Defaulting to UNKNOWN.", value);
            return UNKNOWN;
        }
    }
}
