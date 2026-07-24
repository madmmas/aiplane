package dev.madmmas.aimanager.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Lookup table for per-model token rates. Unknown models resolve to cost {@code 0} (first sighting
 * logged at WARN, subsequent at DEBUG).
 */
@Component
public class CostRateRegistry {

  /** Matches {@code usage_events.cost_usd NUMERIC(16,8)}. */
  public static final int COST_SCALE = 8;

  public static final RoundingMode COST_ROUNDING = RoundingMode.HALF_UP;

  private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
  private static final Logger log = LoggerFactory.getLogger(CostRateRegistry.class);

  private final Map<String, Rate> ratesByModel;
  private final Set<String> warnedUnknownModels = ConcurrentHashMap.newKeySet();

  public CostRateRegistry(CostRateProperties properties) {
    Map<String, Rate> map = new LinkedHashMap<>();
    for (CostRateProperties.ModelRate entry : properties.getRates()) {
      if (entry.getModel() == null || entry.getModel().isBlank()) {
        continue;
      }
      String key = entry.getModel().trim();
      map.put(
          key,
          new Rate(
              key,
              entry.getInputUsdPer1k().setScale(COST_SCALE, COST_ROUNDING),
              entry.getOutputUsdPer1k().setScale(COST_SCALE, COST_ROUNDING)));
    }
    this.ratesByModel = Collections.unmodifiableMap(map);
  }

  public Optional<Rate> findRate(String model) {
    if (model == null || model.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(ratesByModel.get(model.trim()));
  }

  public Map<String, Rate> allRates() {
    return ratesByModel;
  }

  /**
   * {@code (inputTokens/1000)*inputUsdPer1k + (outputTokens/1000)*outputUsdPer1k}, scale 8. Unknown
   * model → {@code 0}.
   */
  public BigDecimal computeCost(String model, int inputTokens, int outputTokens) {
    Optional<Rate> rate = findRate(model);
    if (rate.isEmpty()) {
      String key = model == null ? "" : model.trim();
      if (!key.isEmpty() && warnedUnknownModels.add(key)) {
        log.warn("No cost rate configured for model '{}'; treating costUsd as 0", key);
      } else if (!key.isEmpty()) {
        log.debug("No cost rate configured for model '{}'; treating costUsd as 0", key);
      }
      return BigDecimal.ZERO.setScale(COST_SCALE, COST_ROUNDING);
    }

    Rate r = rate.get();
    BigDecimal inputCost =
        BigDecimal.valueOf(inputTokens)
            .multiply(r.inputUsdPer1k())
            .divide(THOUSAND, COST_SCALE + 4, COST_ROUNDING);
    BigDecimal outputCost =
        BigDecimal.valueOf(outputTokens)
            .multiply(r.outputUsdPer1k())
            .divide(THOUSAND, COST_SCALE + 4, COST_ROUNDING);
    return inputCost.add(outputCost).setScale(COST_SCALE, COST_ROUNDING);
  }

  public record Rate(String model, BigDecimal inputUsdPer1k, BigDecimal outputUsdPer1k) {}
}
