package dev.madmmas.aimanager.guardrail.advisor;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailAction;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailStage;
import dev.madmmas.aimanager.guardrail.evaluator.GuardrailEvaluatorRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * Spring AI {@link CallAdvisor} that runs {@link
 * dev.madmmas.aimanager.guardrail.GuardrailEvaluator} strategies against prompt and response text.
 *
 * <p>Attach via {@code ChatClient.builder(...).defaultAdvisors(advisor)} or per-call {@code
 * .advisors(advisor)}. Rules with {@link GuardrailAction#BLOCK} short-circuit the model call
 * (input) or replace the model response (output).
 */
public class GuardrailCallAdvisor implements CallAdvisor {

  public static final String CONTEXT_RESULTS_KEY = "aiplane.guardrail.results";
  public static final String CONTEXT_BLOCKED_KEY = "aiplane.guardrail.blocked";

  private final List<GuardrailRule> rules;
  private final GuardrailEvaluatorRegistry registry;
  private final int order;

  public GuardrailCallAdvisor(List<GuardrailRule> rules, GuardrailEvaluatorRegistry registry) {
    this(rules, registry, Ordered.HIGHEST_PRECEDENCE + 100);
  }

  public GuardrailCallAdvisor(
      List<GuardrailRule> rules, GuardrailEvaluatorRegistry registry, int order) {
    Assert.notNull(rules, "rules must not be null");
    Assert.notNull(registry, "registry must not be null");
    this.rules = List.copyOf(rules);
    this.registry = registry;
    this.order = order;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    List<Map<String, Object>> results = new ArrayList<>();
    String inputText = request.prompt().getContents();

    ChatClientRequest workingRequest = request;
    for (GuardrailRule rule : rules) {
      if (!rule.enabled() || !rule.stage().appliesToInput()) {
        continue;
      }
      EvaluationResult result = registry.evaluate(rule, inputText);
      results.add(toContextEntry(rule, GuardrailStage.INPUT, result));
      if (result.shouldBlock()) {
        return blockedResponse(workingRequest, results, failureMessage(rule, result));
      }
      if (!result.passed() && result.action() == GuardrailAction.REDACT && result.redactedText() != null) {
        inputText = result.redactedText();
        workingRequest = withPromptText(workingRequest, inputText);
      }
    }

    ChatClientResponse response = chain.nextCall(workingRequest);
    String outputText = extractAssistantText(response);

    for (GuardrailRule rule : rules) {
      if (!rule.enabled() || !rule.stage().appliesToOutput()) {
        continue;
      }
      EvaluationResult result = registry.evaluate(rule, outputText);
      results.add(toContextEntry(rule, GuardrailStage.OUTPUT, result));
      if (result.shouldBlock()) {
        return blockedResponse(workingRequest, results, failureMessage(rule, result));
      }
      if (!result.passed() && result.action() == GuardrailAction.REDACT && result.redactedText() != null) {
        outputText = result.redactedText();
        response = withAssistantText(response, outputText);
      }
    }

    Map<String, Object> context = new HashMap<>(response.context());
    context.put(CONTEXT_RESULTS_KEY, List.copyOf(results));
    context.put(CONTEXT_BLOCKED_KEY, false);
    return response.mutate().context(context).build();
  }

  private static ChatClientRequest withPromptText(ChatClientRequest request, String text) {
    return request.mutate().prompt(new Prompt(text)).build();
  }

  private static ChatClientResponse withAssistantText(ChatClientResponse response, String text) {
    return response
        .mutate()
        .chatResponse(
            ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(text))))
                .build())
        .build();
  }

  private static String extractAssistantText(ChatClientResponse response) {
    if (response.chatResponse() == null || response.chatResponse().getResults().isEmpty()) {
      return "";
    }
    Message message = response.chatResponse().getResults().getFirst().getOutput();
    return message != null ? message.getText() : "";
  }

  private ChatClientResponse blockedResponse(
      ChatClientRequest request, List<Map<String, Object>> results, String message) {
    Map<String, Object> context = new HashMap<>(request.context());
    context.put(CONTEXT_RESULTS_KEY, List.copyOf(results));
    context.put(CONTEXT_BLOCKED_KEY, true);
    return ChatClientResponse.builder()
        .chatResponse(
            ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(message))))
                .build())
        .context(context)
        .build();
  }

  private static String failureMessage(GuardrailRule rule, EvaluationResult result) {
    if (rule.blockMessage() != null && !rule.blockMessage().isBlank()) {
      return rule.blockMessage();
    }
    return result.reason().isBlank() ? "Request blocked by guardrail: " + rule.name() : result.reason();
  }

  private static Map<String, Object> toContextEntry(
      GuardrailRule rule, GuardrailStage evaluatedAs, EvaluationResult result) {
    Map<String, Object> entry = new HashMap<>();
    entry.put("ruleId", rule.id());
    entry.put("ruleName", rule.name());
    entry.put("type", rule.type().wireValue());
    entry.put("stage", evaluatedAs.wireValue());
    entry.put("passed", result.passed());
    entry.put("reason", result.reason());
    if (result.action() != null) {
      entry.put("action", result.action().wireValue());
    }
    if (result.matchedFragment() != null) {
      entry.put("matched", result.matchedFragment());
    }
    return entry;
  }
}
