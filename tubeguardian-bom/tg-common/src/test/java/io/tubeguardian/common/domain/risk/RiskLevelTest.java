package io.tubeguardian.common.domain.risk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class RiskLevelTest {

  @Test
  @DisplayName("Should return specific Enum for valid inputs (case insensitive)")
  void testValidInputs() {
    assertThat(RiskLevel.fromString("HIGH")).isEqualTo(RiskLevel.HIGH);
    assertThat(RiskLevel.fromString("medium")).isEqualTo(RiskLevel.MEDIUM); // test lower case
    assertThat(RiskLevel.fromString("  low  ")).isEqualTo(RiskLevel.LOW); // test trim
  }

  @Test
  @DisplayName("Should return NONE for null input")
  void testNullInput() {
    assertThat(RiskLevel.fromString(null)).isEqualTo(RiskLevel.NONE);
  }

  @ParameterizedTest
  @ValueSource(strings = {"EXTREME", "", "JUNK_VALUE"})
  @DisplayName("Should return UNKNOWN for invalid values instead of throwing exception")
  void testInvalidInputs(String invalidInput) {
    assertThat(RiskLevel.fromString(invalidInput)).isEqualTo(RiskLevel.UNKNOWN);
  }
}
