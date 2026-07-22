package dev.madmmas.aimanager.common.util;

/**
 * Normalizes user-facing names into URL-safe slugs (projects, prompts, etc.).
 */
public final class SlugNormalizer {

  private SlugNormalizer() {}

  /**
   * Lowercases, trims, replaces runs of non-alphanumeric characters with a single hyphen, and
   * strips leading/trailing hyphens.
   *
   * @return empty string when {@code input} is null or blank after normalization
   */
  public static String normalize(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }

    String slug =
        input
            .trim()
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");

    return slug;
  }
}
