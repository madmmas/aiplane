package dev.madmmas.aimanager.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.madmmas.aimanager.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class UsageControllerIT extends AbstractPostgresIntegrationTest {

  private static final String PROJECT_ID = "proj_ackloop";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void ingestCreatesEventsAndReturnsEnvelope() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/usage/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "events": [
                            {
                              "projectId": "%s",
                              "provider": "openai",
                              "model": "gpt-4o-mini",
                              "status": "success",
                              "inputTokens": 10,
                              "outputTokens": 20,
                              "latencyMs": 150,
                              "costUsd": 0.002
                            },
                            {
                              "projectId": "%s",
                              "provider": "anthropic",
                              "model": "claude-haiku-4-20250414",
                              "status": "guardrail-blocked",
                              "inputTokens": 5,
                              "outputTokens": 0,
                              "latencyMs": 12
                            }
                          ]
                        }
                        """
                            .formatted(PROJECT_ID, PROJECT_ID)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accepted").value(2))
            .andExpect(jsonPath("$.events.length()").value(2))
            .andExpect(jsonPath("$.events[0].id").exists())
            .andExpect(jsonPath("$.events[0].provider").value("openai"))
            .andExpect(jsonPath("$.events[1].status").value("guardrail-blocked"))
            .andReturn();

    JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
    String id0 = json.get("events").get(0).get("id").asText();
    assertThat(id0).startsWith("ue_");

    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM usage_events WHERE id = ?", Integer.class, id0);
    assertThat(count).isEqualTo(1);
  }

  @Test
  void ingestRejectsEmptyBatch() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/usage/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                { "events": [] }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("non-empty")));
  }

  @Test
  void ingestRejectsMalformedBatchWithErrorList() throws Exception {
    long before =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usage_events", Long.class);

    mockMvc
        .perform(
            post("/api/v1/usage/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "events": [
                        {
                          "projectId": "proj_does_not_exist",
                          "provider": "openai",
                          "model": "gpt-4o",
                          "status": "success"
                        },
                        {
                          "projectId": "%s",
                          "provider": "openai",
                          "model": "gpt-4o",
                          "status": "success",
                          "inputTokens": -1
                        }
                      ]
                    }
                    """
                        .formatted(PROJECT_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isArray())
        .andExpect(jsonPath("$.errors.length()").value(2))
        .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("Unknown projectId")))
        .andExpect(jsonPath("$.errors[1]").value(org.hamcrest.Matchers.containsString("inputTokens")));

    long after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usage_events", Long.class);
    assertThat(after).isEqualTo(before);
  }
}
