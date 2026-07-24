package dev.madmmas.aimanager.guardrail;

import java.util.List;
import java.util.Optional;

public interface GuardrailSetRepository {

  GuardrailSet save(GuardrailSet set);

  Optional<GuardrailSet> findById(String id);

  List<GuardrailSet> findByProjectId(String projectId);

  GuardrailSet update(GuardrailSet set);

  boolean deleteById(String id);

  boolean existsByProjectIdAndName(String projectId, String name);
}
