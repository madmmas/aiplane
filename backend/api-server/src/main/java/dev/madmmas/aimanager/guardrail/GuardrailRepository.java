package dev.madmmas.aimanager.guardrail;

import java.util.List;
import java.util.Optional;

public interface GuardrailRepository {

  Guardrail save(Guardrail guardrail);

  Optional<Guardrail> findById(String id);

  List<Guardrail> findByProjectId(String projectId);

  List<Guardrail> findByIds(List<String> ids);

  Guardrail update(Guardrail guardrail);

  boolean deleteById(String id);

  boolean existsByProjectIdAndName(String projectId, String name);
}
