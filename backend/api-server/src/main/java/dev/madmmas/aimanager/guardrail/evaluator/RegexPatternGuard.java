package dev.madmmas.aimanager.guardrail.evaluator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Bounds user-supplied regex before compile/evaluate to reduce ReDoS risk (see
 * {@code .cursor/rules/security.mdc}).
 */
public final class RegexPatternGuard {

  /** Hard cap on pattern source length. */
  public static final int MAX_PATTERN_LENGTH = 256;

  /**
   * Nested quantifiers / overlapping alternation heuristics that commonly cause catastrophic
   * backtracking. Not a proof of safety — pairs with length + match timeout.
   */
  private static final Pattern DANGEROUS =
      Pattern.compile(
          // (x+)+, (x*)+, (x+)*, (x?)+, etc.
          "\\([^)]*[+*][^)]*\\)[+*{]"
              // a{n,}+ possessive with nested groups already covered; also x++ style via group
              + "|\\(\\?[^)]*\\)[+*]"
              // classic (a|a)+ / (a|ab)+ style overlap inside a quantified group
              + "|\\([^)]*\\|[^)]*\\)[+*{]");

  private RegexPatternGuard() {}

  /**
   * Validate and compile a user pattern.
   *
   * @throws IllegalArgumentException when the pattern is too long, looks dangerous, or is invalid
   */
  public static Pattern compileSafe(String pattern) {
    if (pattern == null || pattern.isBlank()) {
      throw new IllegalArgumentException("Regex pattern must not be blank");
    }
    if (pattern.length() > MAX_PATTERN_LENGTH) {
      throw new IllegalArgumentException(
          "Regex pattern exceeds max length of " + MAX_PATTERN_LENGTH);
    }
    if (DANGEROUS.matcher(pattern).find()) {
      throw new IllegalArgumentException(
          "Regex pattern rejected: nested or overlapping quantifiers are not allowed");
    }
    try {
      return Pattern.compile(pattern);
    } catch (PatternSyntaxException ex) {
      throw new IllegalArgumentException("Invalid regex pattern: " + ex.getDescription(), ex);
    }
  }
}
