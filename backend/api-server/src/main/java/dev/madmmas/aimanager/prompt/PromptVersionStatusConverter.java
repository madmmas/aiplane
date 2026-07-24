package dev.madmmas.aimanager.prompt;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PromptVersionStatusConverter
    implements AttributeConverter<PromptVersionStatus, String> {

  @Override
  public String convertToDatabaseColumn(PromptVersionStatus attribute) {
    return attribute == null ? null : attribute.wireValue();
  }

  @Override
  public PromptVersionStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : PromptVersionStatus.fromWireValue(dbData);
  }
}
