package dev.madmmas.aimanager.common.util;

import java.util.UUID;

/** Generates prefixed opaque IDs for domain rows (no JPA @GeneratedValue yet). */
public final class Ids {

  private Ids() {}

  public static String next(String prefix) {
    return prefix + UUID.randomUUID().toString().replace("-", "");
  }
}
