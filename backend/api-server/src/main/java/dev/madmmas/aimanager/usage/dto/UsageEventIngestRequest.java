package dev.madmmas.aimanager.usage.dto;

import java.util.List;

/** Forward-compatible envelope for batched ingest: {@code { "events": [ ... ] }}. */
public record UsageEventIngestRequest(List<UsageEventCreateRequest> events) {}
