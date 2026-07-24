package dev.madmmas.aimanager.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PromptVersionStatusTests {

  @ParameterizedTest
  @CsvSource({
    "DRAFT, TESTING, true",
    "TESTING, ACTIVE, true",
    "ACTIVE, ARCHIVED, true",
    "DRAFT, ACTIVE, false",
    "DRAFT, ARCHIVED, false",
    "TESTING, DRAFT, false",
    "TESTING, ARCHIVED, false",
    "ACTIVE, TESTING, false",
    "ACTIVE, DRAFT, false",
    "ARCHIVED, DRAFT, false",
    "ARCHIVED, TESTING, false",
    "ARCHIVED, ACTIVE, false",
    "DRAFT, DRAFT, false",
    "ACTIVE, ACTIVE, false"
  })
  void canTransitionToEnforcesPromotionPath(
      PromptVersionStatus from, PromptVersionStatus to, boolean allowed) {
    assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
  }

  @Test
  void nextPromotionStatusAdvancesOneStep() {
    assertThat(PromptVersionStatus.DRAFT.nextPromotionStatus())
        .isEqualTo(PromptVersionStatus.TESTING);
    assertThat(PromptVersionStatus.TESTING.nextPromotionStatus())
        .isEqualTo(PromptVersionStatus.ACTIVE);
    assertThat(PromptVersionStatus.ACTIVE.nextPromotionStatus()).isNull();
    assertThat(PromptVersionStatus.ARCHIVED.nextPromotionStatus()).isNull();
  }
}
