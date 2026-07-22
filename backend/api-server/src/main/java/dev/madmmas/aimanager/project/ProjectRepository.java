package dev.madmmas.aimanager.project;

import java.util.List;

/** Persistence port for the {@code projects} table. */
public interface ProjectRepository {

  long count();

  List<String> findAllSlugs();

  boolean existsBySlug(String slug);
}
