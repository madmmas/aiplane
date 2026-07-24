package dev.madmmas.aimanager.prompt;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromptVersionRepository extends JpaRepository<PromptVersion, String> {

  List<PromptVersion> findByPromptIdOrderByVersionDesc(String promptId);

  Optional<PromptVersion> findByIdAndPromptId(String id, String promptId);

  List<PromptVersion> findByPromptIdAndStatus(String promptId, PromptVersionStatus status);

  @Query(
      "select coalesce(max(v.version), 0) from PromptVersion v where v.promptId = :promptId")
  int findMaxVersionByPromptId(@Param("promptId") String promptId);
}
