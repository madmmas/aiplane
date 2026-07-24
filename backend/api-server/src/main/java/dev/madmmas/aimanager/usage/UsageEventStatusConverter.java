package dev.madmmas.aimanager.usage;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UsageEventStatusConverter implements AttributeConverter<UsageEventStatus, String> {

  @Override
  public String convertToDatabaseColumn(UsageEventStatus attribute) {
    return attribute == null ? null : attribute.wireValue();
  }

  @Override
  public UsageEventStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : UsageEventStatus.fromWireValue(dbData);
  }
}
