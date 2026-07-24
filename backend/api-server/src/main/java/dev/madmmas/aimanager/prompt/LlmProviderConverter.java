package dev.madmmas.aimanager.prompt;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LlmProviderConverter implements AttributeConverter<LlmProvider, String> {

  @Override
  public String convertToDatabaseColumn(LlmProvider attribute) {
    return attribute == null ? null : attribute.wireValue();
  }

  @Override
  public LlmProvider convertToEntityAttribute(String dbData) {
    return dbData == null ? null : LlmProvider.fromWireValue(dbData);
  }
}
