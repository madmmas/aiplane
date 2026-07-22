export type ApiClientConfig = {
  /** Backend origin, e.g. `http://localhost:8080`. */
  baseUrl: string;
  /** Returns the current JWT access token for `Authorization: Bearer …`. */
  getAccessToken?: () => string | null | undefined;
  /**
   * When true (default), React Query hooks return in-memory mock data.
   * Flip to false once the Spring API is available.
   */
  useMocks?: boolean;
  /** Optional `fetch` override (tests). */
  fetch?: typeof fetch;
};

export class ApiError extends Error {
  readonly status: number;
  readonly body: unknown;

  constructor(message: string, status: number, body: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

export type ApiRequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  /** Skip auth header injection for public endpoints (`/auth/**`). */
  skipAuth?: boolean;
  query?: Record<string, string | number | boolean | undefined | null>;
};

function buildUrl(baseUrl: string, path: string, query?: ApiRequestOptions["query"]): string {
  const normalizedBase = baseUrl.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const url = new URL(`${normalizedBase}${normalizedPath}`);

  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value === undefined || value === null) continue;
      url.searchParams.set(key, String(value));
    }
  }

  return url.toString();
}

export function createApiClient(config: ApiClientConfig) {
  const useMocks = config.useMocks ?? true;
  const fetchImpl = config.fetch ?? globalThis.fetch.bind(globalThis);

  async function apiFetch<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
    const { body, skipAuth, query, headers: initHeaders, ...rest } = options;
    const headers = new Headers(initHeaders);

    if (body !== undefined && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }

    if (!skipAuth) {
      const token = config.getAccessToken?.();
      if (token) {
        headers.set("Authorization", `Bearer ${token}`);
      }
    }

    const response = await fetchImpl(buildUrl(config.baseUrl, path, query), {
      ...rest,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });

    if (response.status === 204) {
      return undefined as T;
    }

    const text = await response.text();
    const parsed: unknown = text ? safeJson(text) : undefined;

    if (!response.ok) {
      throw new ApiError(
        `API ${response.status} ${response.statusText} for ${path}`,
        response.status,
        parsed,
      );
    }

    return parsed as T;
  }

  return {
    config: { ...config, useMocks },
    apiFetch,
  };
}

export type ApiClient = ReturnType<typeof createApiClient>;

function safeJson(text: string): unknown {
  try {
    return JSON.parse(text) as unknown;
  } catch {
    return text;
  }
}
