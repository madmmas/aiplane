package dev.madmmas.aimanager.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-model token rates loaded from {@code aiplane.cost-rates.rates} in application.yml. Amounts
 * are USD per 1K tokens (input / output).
 */
@ConfigurationProperties(prefix = "aiplane.cost-rates")
public class CostRateProperties {

  private List<ModelRate> rates = new ArrayList<>();

  public List<ModelRate> getRates() {
    return rates;
  }

  public void setRates(List<ModelRate> rates) {
    this.rates = rates == null ? new ArrayList<>() : rates;
  }

  public static class ModelRate {
    private String model = "";
    private BigDecimal inputUsdPer1k = BigDecimal.ZERO;
    private BigDecimal outputUsdPer1k = BigDecimal.ZERO;

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model == null ? "" : model;
    }

    public BigDecimal getInputUsdPer1k() {
      return inputUsdPer1k;
    }

    public void setInputUsdPer1k(BigDecimal inputUsdPer1k) {
      this.inputUsdPer1k = inputUsdPer1k == null ? BigDecimal.ZERO : inputUsdPer1k;
    }

    public BigDecimal getOutputUsdPer1k() {
      return outputUsdPer1k;
    }

    public void setOutputUsdPer1k(BigDecimal outputUsdPer1k) {
      this.outputUsdPer1k = outputUsdPer1k == null ? BigDecimal.ZERO : outputUsdPer1k;
    }
  }
}
