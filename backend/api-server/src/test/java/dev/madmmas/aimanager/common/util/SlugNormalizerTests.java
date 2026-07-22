package dev.madmmas.aimanager.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SlugNormalizerTests {

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  void normalizeReturnsEmptyForBlankInput(String input) {
    assertThat(SlugNormalizer.normalize(input)).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
    "News Radar, news-radar",
    "Ackloop, ackloop",
    "Hello___World!!, hello-world",
    "  Mixed CASE  Name  , mixed-case-name"
  })
  void normalizeProducesUrlSafeSlugs(String input, String expected) {
    assertThat(SlugNormalizer.normalize(input)).isEqualTo(expected);
  }

  @Test
  void normalizeStripsLeadingAndTrailingSeparators() {
    assertThat(SlugNormalizer.normalize("---edge---")).isEqualTo("edge");
  }
}
