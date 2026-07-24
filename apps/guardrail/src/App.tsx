import {
  useCreateGuardrail,
  useCreateGuardrailSet,
  useEvaluateGuardrailSet,
  useGuardrailSets,
  useGuardrails,
  useProjects,
  useUpdateGuardrailSet,
} from "@repo/api-client";
import type {
  EvaluatorResult,
  Guardrail,
  GuardrailAction,
  GuardrailStage,
  GuardrailType,
} from "@repo/types";
import { Badge, Button, Card, CardContent, CardHeader, CardTitle, Input } from "@repo/ui";
import { type FormEvent, useEffect, useMemo, useState } from "react";

const EVALUATOR_TYPES: GuardrailType[] = ["keyword-blocklist", "regex-filter", "max-length"];
const STAGES: GuardrailStage[] = ["input", "output", "both"];
const ACTIONS: GuardrailAction[] = ["block", "warn", "redact", "log-only"];

function buildConfig(type: GuardrailType, raw: string): Record<string, unknown> {
  if (type === "keyword-blocklist") {
    return {
      keywords: raw
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean),
    };
  }
  if (type === "regex-filter") {
    return {
      patterns: raw
        .split("\n")
        .map((s) => s.trim())
        .filter(Boolean),
    };
  }
  const maxChars = Number(raw);
  if (!Number.isFinite(maxChars) || maxChars < 0) {
    throw new Error("maxChars must be a non-negative number");
  }
  return { maxChars };
}

function configHint(type: GuardrailType): string {
  switch (type) {
    case "keyword-blocklist":
      return "Comma-separated keywords";
    case "regex-filter":
      return "One regex pattern per line";
    case "max-length":
      return "Max character count";
    default:
      return "Config";
  }
}

function moveItem<T>(items: T[], index: number, direction: -1 | 1): T[] {
  const target = index + direction;
  if (target < 0 || target >= items.length) return items;
  const next = [...items];
  const [removed] = next.splice(index, 1);
  next.splice(target, 0, removed);
  return next;
}

export default function App() {
  const { data: projects = [] } = useProjects();
  const [projectId, setProjectId] = useState<string>("");
  const effectiveProjectId = projectId || projects[0]?.id || "";

  const { data: guardrails = [], isLoading: loadingRules } = useGuardrails({
    projectId: effectiveProjectId,
  });
  const { data: sets = [], isLoading: loadingSets } = useGuardrailSets({
    projectId: effectiveProjectId,
  });

  const createGuardrail = useCreateGuardrail();
  const createSet = useCreateGuardrailSet();
  const updateSet = useUpdateGuardrailSet();
  const evaluateSet = useEvaluateGuardrailSet();

  const [ruleName, setRuleName] = useState("");
  const [ruleType, setRuleType] = useState<GuardrailType>("keyword-blocklist");
  const [ruleStage, setRuleStage] = useState<GuardrailStage>("input");
  const [ruleAction, setRuleAction] = useState<GuardrailAction>("block");
  const [ruleConfig, setRuleConfig] = useState("secret, classified");
  const [ruleError, setRuleError] = useState<string | null>(null);

  const [setName, setSetName] = useState("");
  const [selectedSetId, setSelectedSetId] = useState<string>("");
  const selectedSet = useMemo(
    () => sets.find((s) => s.id === selectedSetId) ?? sets[0],
    [sets, selectedSetId],
  );
  const effectiveSetId = selectedSet?.id ?? "";

  const [memberIds, setMemberIds] = useState<string[]>([]);
  useEffect(() => {
    setMemberIds(selectedSet?.guardrailIds ?? []);
  }, [selectedSet]);

  const [inputText, setInputText] = useState("This contains a secret token.");
  const [outputText, setOutputText] = useState("");
  const [runAll, setRunAll] = useState(false);
  const [results, setResults] = useState<EvaluatorResult[]>([]);
  const [evalMeta, setEvalMeta] = useState<{ blocked: boolean; shortCircuited: boolean } | null>(
    null,
  );

  const guardrailById = useMemo(() => {
    const map = new Map<string, Guardrail>();
    for (const g of guardrails) map.set(g.id, g);
    return map;
  }, [guardrails]);

  async function onCreateRule(event: FormEvent) {
    event.preventDefault();
    setRuleError(null);
    if (!effectiveProjectId) {
      setRuleError("Select a project first");
      return;
    }
    try {
      const config = buildConfig(ruleType, ruleConfig);
      await createGuardrail.mutateAsync({
        projectId: effectiveProjectId,
        name: ruleName.trim(),
        type: ruleType,
        stage: ruleStage,
        action: ruleAction,
        config,
      });
      setRuleName("");
    } catch (error) {
      setRuleError(error instanceof Error ? error.message : "Failed to create rule");
    }
  }

  async function onCreateSet(event: FormEvent) {
    event.preventDefault();
    if (!effectiveProjectId || !setName.trim()) return;
    const created = await createSet.mutateAsync({
      projectId: effectiveProjectId,
      name: setName.trim(),
      shortCircuitOnBlock: true,
      guardrailIds: [],
    });
    setSetName("");
    setSelectedSetId(created.id);
  }

  async function onSaveOrdering() {
    if (!effectiveSetId) return;
    await updateSet.mutateAsync({ id: effectiveSetId, guardrailIds: memberIds });
  }

  function onAddMember(guardrailId: string) {
    if (!guardrailId || memberIds.includes(guardrailId)) return;
    setMemberIds((prev) => [...prev, guardrailId]);
  }

  async function onRunTest() {
    if (!effectiveSetId) return;
    const response = await evaluateSet.mutateAsync({
      id: effectiveSetId,
      input: inputText,
      output: outputText,
      shortCircuitOnBlock: runAll ? false : undefined,
    });
    setResults(response.results);
    setEvalMeta({ blocked: response.blocked, shortCircuited: response.shortCircuited });
  }

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6 p-4">
      <header className="space-y-2">
        <h2 className="text-2xl font-semibold tracking-tight">Guardrails</h2>
        <p className="text-sm text-muted-foreground">
          Compose evaluator rules, order them in a set, and test sample text before saving.
        </p>
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
      </header>

      <section aria-labelledby="rules-heading" className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle id="rules-heading">Rules</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {loadingRules ? (
              <p className="text-sm text-muted-foreground">Loading rules…</p>
            ) : guardrails.length === 0 ? (
              <p className="text-sm text-muted-foreground">No rules yet for this project.</p>
            ) : (
              <ul className="space-y-2">
                {guardrails.map((rule) => (
                  <li
                    key={rule.id}
                    className="flex flex-wrap items-center justify-between gap-2 rounded-md border border-border px-3 py-2"
                  >
                    <div>
                      <p className="font-medium">{rule.name}</p>
                      <p className="text-xs text-muted-foreground">
                        {rule.type} · {rule.stage} · {rule.action}
                      </p>
                    </div>
                    <Badge variant={rule.enabled ? "default" : "secondary"}>
                      {rule.enabled ? "enabled" : "disabled"}
                    </Badge>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Create rule</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-3" onSubmit={onCreateRule}>
              <label className="flex flex-col gap-1 text-sm" htmlFor="guardrail-rule-name">
                <span className="font-medium">Name</span>
                <Input
                  id="guardrail-rule-name"
                  required
                  value={ruleName}
                  onChange={(e) => setRuleName(e.target.value)}
                  placeholder="block-secrets"
                />
              </label>
              <div className="grid grid-cols-3 gap-2">
                <label className="flex flex-col gap-1 text-sm">
                  <span className="font-medium">Type</span>
                  <select
                    aria-label="Evaluator type"
                    className="h-9 rounded-md border border-border bg-background px-2"
                    value={ruleType}
                    onChange={(e) => {
                      const next = e.target.value as GuardrailType;
                      setRuleType(next);
                      setRuleConfig(
                        next === "keyword-blocklist"
                          ? "secret, classified"
                          : next === "regex-filter"
                            ? "\\d{3}-\\d{2}-\\d{4}"
                            : "500",
                      );
                    }}
                  >
                    {EVALUATOR_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="flex flex-col gap-1 text-sm">
                  <span className="font-medium">Stage</span>
                  <select
                    aria-label="Stage"
                    className="h-9 rounded-md border border-border bg-background px-2"
                    value={ruleStage}
                    onChange={(e) => setRuleStage(e.target.value as GuardrailStage)}
                  >
                    {STAGES.map((stage) => (
                      <option key={stage} value={stage}>
                        {stage}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="flex flex-col gap-1 text-sm">
                  <span className="font-medium">Action</span>
                  <select
                    aria-label="Action"
                    className="h-9 rounded-md border border-border bg-background px-2"
                    value={ruleAction}
                    onChange={(e) => setRuleAction(e.target.value as GuardrailAction)}
                  >
                    {ACTIONS.map((action) => (
                      <option key={action} value={action}>
                        {action}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              <label className="flex flex-col gap-1 text-sm">
                <span className="font-medium">{configHint(ruleType)}</span>
                <textarea
                  aria-label="Rule config"
                  className="min-h-20 rounded-md border border-border bg-background px-3 py-2 text-sm"
                  value={ruleConfig}
                  onChange={(e) => setRuleConfig(e.target.value)}
                />
              </label>
              {ruleError ? (
                <p className="text-sm text-destructive" role="alert">
                  {ruleError}
                </p>
              ) : null}
              <Button type="submit" disabled={createGuardrail.isPending}>
                {createGuardrail.isPending ? "Saving…" : "Add rule"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </section>

      <section aria-labelledby="sets-heading" className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle id="sets-heading">Guardrail sets</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {loadingSets ? (
              <p className="text-sm text-muted-foreground">Loading sets…</p>
            ) : (
              <label className="flex flex-col gap-1 text-sm">
                <span className="font-medium">Active set</span>
                <select
                  aria-label="Guardrail set"
                  className="h-9 rounded-md border border-border bg-background px-3"
                  value={effectiveSetId}
                  onChange={(e) => setSelectedSetId(e.target.value)}
                >
                  {sets.map((set) => (
                    <option key={set.id} value={set.id}>
                      {set.name}
                    </option>
                  ))}
                </select>
              </label>
            )}
            <form className="flex gap-2" onSubmit={onCreateSet}>
              <Input
                aria-label="New set name"
                placeholder="production-input"
                value={setName}
                onChange={(e) => setSetName(e.target.value)}
              />
              <Button type="submit" variant="secondary" disabled={createSet.isPending}>
                Create set
              </Button>
            </form>

            <div className="space-y-2">
              <p className="text-sm font-medium">Member order</p>
              {memberIds.length === 0 ? (
                <p className="text-sm text-muted-foreground">No members — add a rule below.</p>
              ) : (
                <ol className="space-y-2">
                  {memberIds.map((id, index) => {
                    const rule = guardrailById.get(id);
                    return (
                      <li
                        key={id}
                        className="flex items-center justify-between gap-2 rounded-md border border-border px-3 py-2"
                      >
                        <span className="text-sm">
                          {index + 1}. {rule?.name ?? id}
                        </span>
                        <div className="flex gap-1">
                          <Button
                            type="button"
                            size="sm"
                            variant="outline"
                            aria-label={`Move ${rule?.name ?? id} up`}
                            onClick={() => setMemberIds((prev) => moveItem(prev, index, -1))}
                          >
                            Up
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            variant="outline"
                            aria-label={`Move ${rule?.name ?? id} down`}
                            onClick={() => setMemberIds((prev) => moveItem(prev, index, 1))}
                          >
                            Down
                          </Button>
                          <Button
                            type="button"
                            size="sm"
                            variant="ghost"
                            aria-label={`Remove ${rule?.name ?? id}`}
                            onClick={() =>
                              setMemberIds((prev) => prev.filter((memberId) => memberId !== id))
                            }
                          >
                            Remove
                          </Button>
                        </div>
                      </li>
                    );
                  })}
                </ol>
              )}
              <div className="flex flex-wrap gap-2">
                <select
                  aria-label="Add rule to set"
                  className="h-9 min-w-48 rounded-md border border-border bg-background px-2"
                  defaultValue=""
                  onChange={(e) => {
                    onAddMember(e.target.value);
                    e.target.value = "";
                  }}
                >
                  <option value="" disabled>
                    Add rule…
                  </option>
                  {guardrails
                    .filter((g) => !memberIds.includes(g.id))
                    .map((g) => (
                      <option key={g.id} value={g.id}>
                        {g.name}
                      </option>
                    ))}
                </select>
                <Button
                  type="button"
                  onClick={onSaveOrdering}
                  disabled={!effectiveSetId || updateSet.isPending}
                >
                  Save order
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Test panel</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <label className="flex flex-col gap-1 text-sm">
              <span className="font-medium">Sample input</span>
              <textarea
                aria-label="Sample input"
                className="min-h-24 rounded-md border border-border bg-background px-3 py-2 text-sm"
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              <span className="font-medium">Sample output</span>
              <textarea
                aria-label="Sample output"
                className="min-h-20 rounded-md border border-border bg-background px-3 py-2 text-sm"
                value={outputText}
                onChange={(e) => setOutputText(e.target.value)}
              />
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={runAll}
                onChange={(e) => setRunAll(e.target.checked)}
              />
              Run all rules (disable short-circuit)
            </label>
            <Button
              type="button"
              onClick={onRunTest}
              disabled={!effectiveSetId || evaluateSet.isPending}
            >
              {evaluateSet.isPending ? "Evaluating…" : "Run test"}
            </Button>
            {evalMeta ? (
              <p className="text-sm" aria-live="polite">
                {evalMeta.blocked ? "Blocked" : "Passed"}
                {evalMeta.shortCircuited ? " (short-circuited)" : ""}
              </p>
            ) : null}
            {results.length > 0 ? (
              <ul className="space-y-2" aria-label="Evaluator results">
                {results.map((result, index) => (
                  <li
                    key={`${result.guardrailId}-${result.stage}-${index}`}
                    className="rounded-md border border-border px-3 py-2 text-sm"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-medium">
                        {result.name} · {result.stage}
                      </span>
                      <Badge variant={result.passed ? "secondary" : "destructive"}>
                        {result.passed ? "pass" : "fail"}
                      </Badge>
                    </div>
                    {!result.passed ? (
                      <p className="mt-1 text-muted-foreground">{result.reason}</p>
                    ) : null}
                  </li>
                ))}
              </ul>
            ) : null}
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
