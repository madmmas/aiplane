import { useProjects, useUsageEvents, useUsageSummary } from "@repo/api-client";
import type { UsageEvent } from "@repo/types";
import { Badge, Card, CardContent, CardHeader, CardTitle } from "@repo/ui";
import { useMemo, useState } from "react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

type PeriodOption = "7d" | "30d" | string;

function currentMonthPeriod(now = new Date()): string {
  const year = now.getUTCFullYear();
  const month = String(now.getUTCMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

/** Derive inclusive ISO range for `useUsageEvents` from a summary period string. */
function periodToRange(period: string, now = new Date()): { from: string; to: string } {
  const relative = /^(\d+)d$/.exec(period);
  if (relative) {
    const days = Number(relative[1]);
    return {
      from: new Date(now.getTime() - days * 86_400_000).toISOString(),
      to: now.toISOString(),
    };
  }
  if (/^\d{4}-\d{2}$/.test(period)) {
    const [year, month] = period.split("-").map(Number);
    return {
      from: new Date(Date.UTC(year, month - 1, 1)).toISOString(),
      to: new Date(Date.UTC(year, month, 1)).toISOString(),
    };
  }
  return {
    from: new Date(now.getTime() - 7 * 86_400_000).toISOString(),
    to: now.toISOString(),
  };
}

function dayKey(iso: string): string {
  return iso.slice(0, 10);
}

function groupEventsByDay(
  events: UsageEvent[],
): { date: string; requests: number; costUsd: number }[] {
  const map = new Map<string, { date: string; requests: number; costUsd: number }>();
  for (const event of events) {
    const date = dayKey(event.timestamp);
    const row = map.get(date) ?? { date, requests: 0, costUsd: 0 };
    row.requests += 1;
    row.costUsd += event.costUsd;
    map.set(date, row);
  }
  return [...map.values()]
    .map((row) => ({
      ...row,
      costUsd: Math.round(row.costUsd * 1e6) / 1e6,
    }))
    .sort((a, b) => a.date.localeCompare(b.date));
}

/**
 * Avg latency is not on UsageSummary — compute from the events list for the
 * same period (client-side) rather than extending the backend DTO for MVP.
 */
function averageLatencyMs(events: UsageEvent[]): number | null {
  if (events.length === 0) return null;
  const total = events.reduce((sum, e) => sum + e.latencyMs, 0);
  return Math.round(total / events.length);
}

function formatUsd(value: number): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  }).format(value);
}

function formatLatency(ms: number | null): string {
  if (ms === null) return "—";
  return `${ms} ms`;
}

export default function App() {
  const { data: projects = [] } = useProjects();
  const [projectId, setProjectId] = useState("");
  const [period, setPeriod] = useState<PeriodOption>("7d");

  const effectiveProjectId = projectId || projects[0]?.id || "";
  const monthPeriod = currentMonthPeriod();
  const range = useMemo(() => periodToRange(period), [period]);

  const {
    data: summary,
    isLoading: loadingSummary,
    isError: summaryError,
  } = useUsageSummary({
    projectId: effectiveProjectId,
    period,
  });

  const {
    data: events = [],
    isLoading: loadingEvents,
    isError: eventsError,
  } = useUsageEvents({
    projectId: effectiveProjectId,
    from: range.from,
    to: range.to,
  });

  const avgLatency = useMemo(() => averageLatencyMs(events), [events]);
  const chartData = useMemo(() => groupEventsByDay(events), [events]);
  const maxProviderCost = useMemo(
    () => Math.max(0, ...(summary?.byProvider.map((p) => p.costUsd) ?? [0])),
    [summary],
  );

  const loading = loadingSummary || loadingEvents;
  const hasError = summaryError || eventsError;

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6 p-4">
      <header className="space-y-2">
        <h2 className="text-2xl font-semibold tracking-tight">Usage</h2>
        <p className="text-sm text-muted-foreground">
          Usage overview — requests, cost, and latency for the selected project and period.
        </p>
        <div className="flex flex-wrap gap-4">
          <label className="flex max-w-sm flex-col gap-1 text-sm">
            <span className="font-medium">Project</span>
            <select
              aria-label="Project"
              className="h-9 rounded-md border border-border bg-background px-3"
              value={effectiveProjectId}
              onChange={(e) => setProjectId(e.target.value)}
            >
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </label>
          <label className="flex max-w-xs flex-col gap-1 text-sm">
            <span className="font-medium">Period</span>
            <select
              aria-label="Period"
              className="h-9 rounded-md border border-border bg-background px-3"
              value={period}
              onChange={(e) => setPeriod(e.target.value)}
            >
              <option value="7d">Last 7 days</option>
              <option value="30d">Last 30 days</option>
              <option value={monthPeriod}>This month ({monthPeriod})</option>
            </select>
          </label>
        </div>
      </header>

      {hasError ? (
        <p className="text-sm text-destructive" role="alert">
          Failed to load usage data. Try again or check the API connection.
        </p>
      ) : null}

      <section aria-labelledby="usage-kpis-heading" className="grid gap-4 sm:grid-cols-3">
        <h3 id="usage-kpis-heading" className="sr-only">
          Key metrics
        </h3>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total requests
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold tabular-nums" aria-live="polite">
              {loading ? "…" : (summary?.totalRequests ?? 0).toLocaleString()}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Total cost</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold tabular-nums" aria-live="polite">
              {loading ? "…" : formatUsd(summary?.totalCostUsd ?? 0)}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Avg latency</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold tabular-nums" aria-live="polite">
              {loading ? "…" : formatLatency(avgLatency)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              From events in range (not in summary API)
            </p>
          </CardContent>
        </Card>
      </section>

      <section aria-labelledby="provider-breakdown-heading" className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle id="provider-breakdown-heading">By provider</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-sm text-muted-foreground">Loading breakdown…</p>
            ) : !summary?.byProvider.length ? (
              <p className="text-sm text-muted-foreground">No usage in this period.</p>
            ) : (
              <ul className="space-y-3" aria-label="Provider breakdown">
                {summary.byProvider.map((row) => {
                  const widthPct =
                    maxProviderCost > 0 ? Math.max(4, (row.costUsd / maxProviderCost) * 100) : 0;
                  return (
                    <li key={row.provider} className="space-y-1">
                      <div className="flex items-center justify-between gap-2 text-sm">
                        <span className="font-medium">{row.provider}</span>
                        <Badge variant="secondary">{row.requests} req</Badge>
                      </div>
                      <div
                        className="h-2 rounded-full bg-muted"
                        role="img"
                        aria-label={`${row.provider} cost ${formatUsd(row.costUsd)}`}
                      >
                        <div
                          className="h-2 rounded-full bg-foreground/70"
                          style={{ width: `${widthPct}%` }}
                        />
                      </div>
                      <p className="text-xs text-muted-foreground">{formatUsd(row.costUsd)}</p>
                    </li>
                  );
                })}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle id="usage-chart-heading">Usage over time</CardTitle>
          </CardHeader>
          <CardContent>
            <div
              role="img"
              aria-labelledby="usage-chart-heading"
              aria-label="Usage over time"
              className="h-64 w-full"
            >
              {loading ? (
                <p className="text-sm text-muted-foreground">Loading chart…</p>
              ) : chartData.length === 0 ? (
                <p className="text-sm text-muted-foreground">No events to chart for this period.</p>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                    <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                    <YAxis yAxisId="left" tick={{ fontSize: 12 }} allowDecimals={false} />
                    <YAxis
                      yAxisId="right"
                      orientation="right"
                      tick={{ fontSize: 12 }}
                      tickFormatter={(v: number) => `$${v}`}
                    />
                    <Tooltip />
                    <Legend />
                    <Area
                      yAxisId="left"
                      type="monotone"
                      dataKey="requests"
                      name="Requests"
                      stroke="hsl(var(--foreground))"
                      fill="hsl(var(--foreground) / 0.15)"
                      strokeWidth={2}
                    />
                    <Area
                      yAxisId="right"
                      type="monotone"
                      dataKey="costUsd"
                      name="Cost (USD)"
                      stroke="hsl(var(--muted-foreground))"
                      fill="hsl(var(--muted-foreground) / 0.12)"
                      strokeWidth={2}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              )}
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
