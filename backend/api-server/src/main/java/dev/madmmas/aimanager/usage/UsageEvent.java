package dev.madmmas.aimanager.usage;

import dev.madmmas.aimanager.prompt.LlmProvider;
import dev.madmmas.aimanager.prompt.LlmProviderConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "usage_events")
public class UsageEvent {

  @Id
  @Column(length = 64)
  private String id;

  @Column(name = "project_id", nullable = false, length = 64)
  private String projectId;

  @Column(name = "prompt_id", length = 64)
  private String promptId;

  @Column(name = "prompt_version_id", length = 64)
  private String promptVersionId;

  @Column(name = "api_key_id", length = 64)
  private String apiKeyId;

  @Convert(converter = LlmProviderConverter.class)
  @Column(nullable = false, length = 64)
  private LlmProvider provider;

  @Column(nullable = false, length = 128)
  private String model;

  @Column(name = "input_tokens", nullable = false)
  private int inputTokens;

  @Column(name = "output_tokens", nullable = false)
  private int outputTokens;

  @Column(name = "latency_ms", nullable = false)
  private int latencyMs;

  @Column(name = "cost_usd", nullable = false, precision = 16, scale = 8)
  private BigDecimal costUsd = BigDecimal.ZERO;

  @Convert(converter = UsageEventStatusConverter.class)
  @Column(nullable = false, length = 32)
  private UsageEventStatus status;

  @Column(nullable = false)
  private Instant timestamp;

  @PrePersist
  void onCreate() {
    if (timestamp == null) {
      timestamp = Instant.now();
    }
    if (costUsd == null) {
      costUsd = BigDecimal.ZERO;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getPromptId() {
    return promptId;
  }

  public void setPromptId(String promptId) {
    this.promptId = promptId;
  }

  public String getPromptVersionId() {
    return promptVersionId;
  }

  public void setPromptVersionId(String promptVersionId) {
    this.promptVersionId = promptVersionId;
  }

  public String getApiKeyId() {
    return apiKeyId;
  }

  public void setApiKeyId(String apiKeyId) {
    this.apiKeyId = apiKeyId;
  }

  public LlmProvider getProvider() {
    return provider;
  }

  public void setProvider(LlmProvider provider) {
    this.provider = provider;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public int getInputTokens() {
    return inputTokens;
  }

  public void setInputTokens(int inputTokens) {
    this.inputTokens = inputTokens;
  }

  public int getOutputTokens() {
    return outputTokens;
  }

  public void setOutputTokens(int outputTokens) {
    this.outputTokens = outputTokens;
  }

  public int getLatencyMs() {
    return latencyMs;
  }

  public void setLatencyMs(int latencyMs) {
    this.latencyMs = latencyMs;
  }

  public BigDecimal getCostUsd() {
    return costUsd;
  }

  public void setCostUsd(BigDecimal costUsd) {
    this.costUsd = costUsd;
  }

  public UsageEventStatus getStatus() {
    return status;
  }

  public void setStatus(UsageEventStatus status) {
    this.status = status;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
