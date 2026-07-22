export type NavId = "prompts" | "guardrails" | "users" | "usage" | "providers" | "apiKeys";

export type RemoteNavId = Extract<NavId, "prompts" | "guardrails" | "users" | "usage">;

export function isRemoteNav(id: NavId): id is RemoteNavId {
  return id === "prompts" || id === "guardrails" || id === "users" || id === "usage";
}
