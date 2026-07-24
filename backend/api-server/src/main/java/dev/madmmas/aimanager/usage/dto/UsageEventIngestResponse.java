package dev.madmmas.aimanager.usage.dto;

import java.util.List;

public record UsageEventIngestResponse(int accepted, List<UsageEventResponse> events) {}
