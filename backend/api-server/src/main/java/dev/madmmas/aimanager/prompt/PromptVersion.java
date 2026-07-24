package dev.madmmas.aimanager.prompt;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "prompt_versions")
public class PromptVersion {

  @Id
  @Column(length = 64)
  private String id;

  @Column(name = "prompt_id", nullable = false, length = 64)
  private String promptId;

  @Column(nullable = false)
  private int version;

  @Column(length = 128)
  private String label;

  @Column(nullable = false, length = 128)
  private String model;

  @Convert(converter = LlmProviderConverter.class)
  @Column(nullable = false, length = 64)
  private LlmProvider provider;

  @Column(name = "system_prompt", nullable = false, columnDefinition = "text")
  private String systemPrompt = "";

  @Column(name = "user_prompt_template", nullable = false, columnDefinition = "text")
  private String userPromptTemplate = "";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> parameters = new HashMap<>();

  @Convert(converter = PromptVersionStatusConverter.class)
  @Column(nullable = false, length = 32)
  private PromptVersionStatus status = PromptVersionStatus.DRAFT;

  @Column(name = "created_by", nullable = false, length = 64)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> metrics;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (systemPrompt == null) {
      systemPrompt = "";
    }
    if (userPromptTemplate == null) {
      userPromptTemplate = "";
    }
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    if (status == null) {
      status = PromptVersionStatus.DRAFT;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPromptId() {
    return promptId;
  }

  public void setPromptId(String promptId) {
    this.promptId = promptId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public LlmProvider getProvider() {
    return provider;
  }

  public void setProvider(LlmProvider provider) {
    this.provider = provider;
  }

  public String getSystemPrompt() {
    return systemPrompt;
  }

  public void setSystemPrompt(String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }

  public String getUserPromptTemplate() {
    return userPromptTemplate;
  }

  public void setUserPromptTemplate(String userPromptTemplate) {
    this.userPromptTemplate = userPromptTemplate;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public PromptVersionStatus getStatus() {
    return status;
  }

  public void setStatus(PromptVersionStatus status) {
    this.status = status;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Map<String, Object> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, Object> metrics) {
    this.metrics = metrics;
  }
}
