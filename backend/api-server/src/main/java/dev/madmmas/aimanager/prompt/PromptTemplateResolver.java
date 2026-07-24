package dev.madmmas.aimanager.prompt;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves {@code {{variable}}} placeholders in prompt templates. */
public final class PromptTemplateResolver {

  private static final Pattern VARIABLE =
      Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*\\}\\}");

  private PromptTemplateResolver() {}

  public static String resolve(String template, Map<String, String> variables) {
    if (template == null || template.isEmpty()) {
      return template == null ? "" : template;
    }
    if (variables == null || variables.isEmpty()) {
      return template;
    }
    Matcher matcher = VARIABLE.matcher(template);
    StringBuilder out = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      String replacement = variables.getOrDefault(key, matcher.group(0));
      matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(out);
    return out.toString();
  }
}
