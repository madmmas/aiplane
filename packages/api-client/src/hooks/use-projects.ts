import type { PageResponse, Project } from "@repo/types";
import { useQuery } from "@tanstack/react-query";
import type { ApiClient } from "../client";
import { useApiClient } from "../context";
import { listMockProjects } from "../mocks";

export const projectKeys = {
  all: ["projects"] as const,
  list: () => [...projectKeys.all, "list"] as const,
};

async function fetchProjects(client: ApiClient): Promise<Project[]> {
  if (client.config.useMocks) {
    return listMockProjects();
  }

  const page = await client.apiFetch<PageResponse<Project> | Project[]>("/api/v1/projects");
  return Array.isArray(page) ? page : page.content;
}

/** List projects for the current user. Returns mock data until the API exists. */
export function useProjects() {
  const client = useApiClient();

  return useQuery({
    queryKey: projectKeys.list(),
    queryFn: () => fetchProjects(client),
  });
}
