package dev.madmmas.aimanager.guardrail.evaluator;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailEvaluator;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Blocks text containing any configured keyword (case-insensitive substring match). */
@Component
public class KeywordBlocklistEvaluator implements GuardrailEvaluator {

  @Override
  public GuardrailType supportedType() {
    return GuardrailType.KEYWORD_BLOCKLIST;
  }

  @Override
  public EvaluationResult evaluate(String text, GuardrailRule rule) {
    List<String> keywords = readKeywords(rule.config());
    if (keywords.isEmpty()) {
      return EvaluationResult.pass();
    }

    String haystack = text == null ? "" : text;
    String lower = haystack.toLowerCase(Locale.ROOT);
    for (String keyword : keywords) {
      if (keyword == null || keyword.isBlank()) {
        continue;
      }
      String needle = keyword.toLowerCase(Locale.ROOT);
      if (lower.contains(needle)) {
        String message =
            rule.blockMessage() != null && !rule.blockMessage().isBlank()
                ? rule.blockMessage()
                : "Blocked keyword: " + keyword;
        String redacted = haystack.replaceAll("(?i)" + PatternQuote.quote(keyword), "[REDACTED]");
        return EvaluationResult.fail(message, rule.action(), keyword, redacted);
      }
    }
    return EvaluationResult.pass();
  }

  @SuppressWarnings("unchecked")
  static List<String> readKeywords(Map<String, Object> config) {
    Object raw = config.get("keywords");
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
    throw new IllegalArgumentException("keyword-blocklist config.keywords must be an array");
  }

  /** Local helper so we do not pull Pattern into the hot path for quoting. */
  private static final class PatternQuote {
    private PatternQuote() {}

    static String quote(String keyword) {
      return java.util.regex.Pattern.quote(keyword);
    }
  }
}
