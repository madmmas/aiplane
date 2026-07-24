package dev.madmmas.aimanager.guardrail.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.madmmas.aimanager.guardrail.GuardrailAction;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailStage;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import dev.madmmas.aimanager.guardrail.evaluator.GuardrailEvaluatorRegistry;
import dev.madmmas.aimanager.guardrail.evaluator.KeywordBlocklistEvaluator;
import dev.madmmas.aimanager.guardrail.evaluator.MaxLengthEvaluator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class GuardrailCallAdvisorTests {

  @Mock private CallAdvisorChain chain;

  private GuardrailEvaluatorRegistry registry;

  @BeforeEach
  void setUp() {
    registry =
        new GuardrailEvaluatorRegistry(
            List.of(new KeywordBlocklistEvaluator(), new MaxLengthEvaluator()));
  }

  @Test
  void blocksModelCallWhenInputFails() {
    GuardrailRule rule =
        new GuardrailRule(
            "r1",
            "secrets",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.INPUT,
            Map.of("keywords", List.of("classified")),
            true,
            GuardrailAction.BLOCK,
            "Nope");

    GuardrailCallAdvisor advisor = new GuardrailCallAdvisor(List.of(rule), registry);
    ChatClientRequest request = ChatClientRequest.builder().prompt(new Prompt("classified docs")).build();

    ChatClientResponse response = advisor.adviseCall(request, chain);

    verify(chain, never()).nextCall(any());
    assertThat(response.context().get(GuardrailCallAdvisor.CONTEXT_BLOCKED_KEY)).isEqualTo(true);
    assertThat(response.chatResponse().getResult().getOutput().getText()).isEqualTo("Nope");
  }

  @Test
  void evaluatesOutputAfterModelCall() {
    GuardrailRule rule =
        new GuardrailRule(
            "r1",
            "len",
            GuardrailType.MAX_LENGTH,
            GuardrailStage.OUTPUT,
            Map.of("maxChars", 5),
            true,
            GuardrailAction.BLOCK,
            "too long");

    when(chain.nextCall(any()))
        .thenReturn(
            ChatClientResponse.builder()
                .chatResponse(
                    ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage("abcdefgh"))))
                        .build())
                .context(Map.of())
                .build());

    GuardrailCallAdvisor advisor = new GuardrailCallAdvisor(List.of(rule), registry);
    ChatClientRequest request = ChatClientRequest.builder().prompt(new Prompt("hi")).build();

    ChatClientResponse response = advisor.adviseCall(request, chain);

    verify(chain).nextCall(any());
    assertThat(response.context().get(GuardrailCallAdvisor.CONTEXT_BLOCKED_KEY)).isEqualTo(true);
    assertThat(response.chatResponse().getResult().getOutput().getText()).isEqualTo("too long");
  }

  @Test
  void passesThroughWhenRulesPass() {
    GuardrailRule rule =
        new GuardrailRule(
            "r1",
            "secrets",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.BOTH,
            Map.of("keywords", List.of("classified")),
            true,
            GuardrailAction.BLOCK,
            null);

    when(chain.nextCall(any()))
        .thenReturn(
            ChatClientResponse.builder()
                .chatResponse(
                    ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage("all clear"))))
                        .build())
                .context(Map.of())
                .build());

    GuardrailCallAdvisor advisor = new GuardrailCallAdvisor(List.of(rule), registry);
    ChatClientRequest request = ChatClientRequest.builder().prompt(new Prompt("hello")).build();

    ChatClientResponse response = advisor.adviseCall(request, chain);

    assertThat(response.context().get(GuardrailCallAdvisor.CONTEXT_BLOCKED_KEY)).isEqualTo(false);
    assertThat(response.chatResponse().getResult().getOutput().getText()).isEqualTo("all clear");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> results =
        (List<Map<String, Object>>) response.context().get(GuardrailCallAdvisor.CONTEXT_RESULTS_KEY);
    assertThat(results).hasSize(2);
  }
}
