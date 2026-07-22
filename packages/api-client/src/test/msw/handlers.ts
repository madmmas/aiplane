import { http, HttpResponse } from "msw";
import { MOCK_PROJECTS, MOCK_PROMPTS } from "../../mocks";

const API_ORIGIN = "http://localhost:8080";

/** Shared MSW handlers for api-client integration-style unit tests. */
export const handlers = [
  http.get(`${API_ORIGIN}/api/v1/projects`, () => {
    return HttpResponse.json({
      content: MOCK_PROJECTS,
      page: 0,
      size: MOCK_PROJECTS.length,
      totalElements: MOCK_PROJECTS.length,
      totalPages: 1,
    });
  }),
  http.get(`${API_ORIGIN}/api/v1/prompts`, ({ request }) => {
    const url = new URL(request.url);
    const projectId = url.searchParams.get("projectId") ?? undefined;
    const prompts = projectId
      ? MOCK_PROMPTS.filter((prompt) => prompt.projectId === projectId)
      : MOCK_PROMPTS;

    return HttpResponse.json({
      content: prompts,
      page: 0,
      size: prompts.length,
      totalElements: prompts.length,
      totalPages: 1,
    });
  }),
];
