package dev.madmmas.aimanager.prompt;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, String> {

  List<Prompt> findByProjectIdOrderByUpdatedAtDesc(String projectId);

  boolean existsByProjectIdAndName(String projectId, String name);

  boolean existsByProjectIdAndNameAndIdNot(String projectId, String name, String id);
}
