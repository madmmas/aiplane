package dev.madmmas.aimanager.guardrail.evaluator;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailEvaluator;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Blocks text matching any configured regex. Patterns are validated via {@link RegexPatternGuard}
 * and matched with a wall-clock timeout to bound ReDoS.
 */
@Component
public class RegexFilterEvaluator implements GuardrailEvaluator {

  /** Per-pattern match budget. */
  public static final long MATCH_TIMEOUT_MS = 100L;

  private final ExecutorService matchExecutor =
      Executors.newCachedThreadPool(
          r -> {
            Thread t = new Thread(r, "guardrail-regex-match");
            t.setDaemon(true);
            return t;
          });

  @Override
  public GuardrailType supportedType() {
    return GuardrailType.REGEX_FILTER;
  }

  @Override
  public EvaluationResult evaluate(String text, GuardrailRule rule) {
    List<String> patterns = readPatterns(rule.config());
    if (patterns.isEmpty()) {
      return EvaluationResult.pass();
    }

    String haystack = text == null ? "" : text;
    for (String source : patterns) {
      Pattern compiled = RegexPatternGuard.compileSafe(source);
      MatchOutcome outcome = matchWithTimeout(compiled, haystack);
      if (outcome.timedOut()) {
        throw new IllegalArgumentException(
            "Regex pattern timed out (possible ReDoS): " + truncate(source));
      }
      if (outcome.matched()) {
        String message =
            rule.blockMessage() != null && !rule.blockMessage().isBlank()
                ? rule.blockMessage()
                : "Blocked by regex: " + truncate(source);
        String redacted =
            compiled.matcher(haystack).replaceAll("[REDACTED]");
        return EvaluationResult.fail(message, rule.action(), outcome.fragment(), redacted);
      }
    }
    return EvaluationResult.pass();
  }

  private MatchOutcome matchWithTimeout(Pattern pattern, String text) {
    Future<MatchOutcome> future =
        matchExecutor.submit(
            () -> {
              Matcher matcher = pattern.matcher(text);
              if (matcher.find()) {
                return MatchOutcome.hit(matcher.group());
              }
              return MatchOutcome.miss();
            });
    try {
      return future.get(MATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (TimeoutException ex) {
      future.cancel(true);
      return MatchOutcome.timeout();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return MatchOutcome.timeout();
    } catch (ExecutionException ex) {
      throw new IllegalArgumentException("Regex evaluation failed: " + ex.getCause().getMessage(), ex);
    }
  }

  @SuppressWarnings("unchecked")
  static List<String> readPatterns(Map<String, Object> config) {
    Object raw = config.get("patterns");
    if (raw == null) {
      return List.of();
    }
    if (raw instanceof List<?> list) {
      List<String> out = new ArrayList<>(list.size());
      for (Object item : list) {
        if (item != null) {
          out.add(String.valueOf(item));
        }
      }
      return out;
    }
    throw new IllegalArgumentException("regex-filter config.patterns must be an array");
  }

  private static String truncate(String pattern) {
    return pattern.length() <= 64 ? pattern : pattern.substring(0, 64) + "...";
  }

  private record MatchOutcome(boolean matched, boolean timedOut, String fragment) {
    static MatchOutcome hit(String fragment) {
      return new MatchOutcome(true, false, fragment);
    }

    static MatchOutcome miss() {
      return new MatchOutcome(false, false, null);
    }

    static MatchOutcome timeout() {
      return new MatchOutcome(false, true, null);
    }
  }
}
