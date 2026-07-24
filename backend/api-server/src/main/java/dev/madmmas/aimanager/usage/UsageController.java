package dev.madmmas.aimanager.usage;

import dev.madmmas.aimanager.usage.dto.UsageEventIngestRequest;
import dev.madmmas.aimanager.usage.dto.UsageEventIngestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Usage telemetry write API. Auth via API key is deferred to Phase 4 — endpoints are open for now
 * (same stance as prompts/guardrails).
 */
@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {

  private final UsageService service;

  public UsageController(UsageService service) {
    this.service = service;
  }

  @PostMapping("/events")
  @ResponseStatus(HttpStatus.CREATED)
  UsageEventIngestResponse ingest(@RequestBody UsageEventIngestRequest request) {
    return service.ingest(request);
  }
}
