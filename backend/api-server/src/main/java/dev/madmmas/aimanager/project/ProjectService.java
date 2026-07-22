package dev.madmmas.aimanager.project;

import dev.madmmas.aimanager.common.util.SlugNormalizer;
import org.springframework.stereotype.Service;

/** Project-facing helpers that sit above {@link ProjectRepository}. */
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  /**
   * Returns whether a normalized slug is already taken.
   *
   * @throws IllegalArgumentException when the raw name cannot be normalized into a slug
   */
  public boolean isSlugTaken(String rawName) {
    String slug = SlugNormalizer.normalize(rawName);
    if (slug.isEmpty()) {
      throw new IllegalArgumentException("Project name must produce a non-empty slug");
    }
    return projectRepository.existsBySlug(slug);
  }
}
