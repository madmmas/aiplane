package dev.madmmas.aimanager.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CostRateRegistryTests {

  private CostRateRegistry registry;

  @BeforeEach
  void setUp() {
    CostRateProperties properties = new CostRateProperties();
    CostRateProperties.ModelRate sonnet = new CostRateProperties.ModelRate();
    sonnet.setModel("claude-sonnet-4-20250514");
    sonnet.setInputUsdPer1k(new BigDecimal("0.003"));
    sonnet.setOutputUsdPer1k(new BigDecimal("0.015"));
    CostRateProperties.ModelRate mini = new CostRateProperties.ModelRate();
    mini.setModel("gpt-4o-mini");
    mini.setInputUsdPer1k(new BigDecimal("0.00015"));
    mini.setOutputUsdPer1k(new BigDecimal("0.0006"));
    properties.setRates(List.of(sonnet, mini));
    registry = new CostRateRegistry(properties);
  }

  @Test
  void findRateReturnsConfiguredModel() {
    assertThat(registry.findRate("claude-sonnet-4-20250514")).isPresent();
    assertThat(registry.findRate("gpt-4o-mini").orElseThrow().inputUsdPer1k())
        .isEqualByComparingTo("0.00015000");
  }

  @Test
  void unknownModelReturnsEmptyAndZeroCost() {
    assertThat(registry.findRate("totally-unknown-model")).isEmpty();
    assertThat(registry.computeCost("totally-unknown-model", 1000, 1000))
        .isEqualByComparingTo("0.00000000");
  }

  @Test
  void computeCostUsesPer1kRates() {
    // 1000 in * 0.003/1k + 2000 out * 0.015/1k = 0.003 + 0.030 = 0.033
    assertThat(registry.computeCost("claude-sonnet-4-20250514", 1000, 2000))
        .isEqualByComparingTo("0.03300000");
  }

  @Test
  void zeroTokensYieldsZeroCost() {
    assertThat(registry.computeCost("gpt-4o-mini", 0, 0)).isEqualByComparingTo("0.00000000");
  }

  @Test
  void fractionalTokensRoundToScaleEight() {
    // 1 in * 0.00015/1k + 1 out * 0.0006/1k = 0.00000015 + 0.00000060 = 0.00000075
    assertThat(registry.computeCost("gpt-4o-mini", 1, 1)).isEqualByComparingTo("0.00000075");
  }
}
