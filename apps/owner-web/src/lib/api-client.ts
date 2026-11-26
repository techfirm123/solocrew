
const API_BASE = "/api/backend";

export interface OwnerWorkspace {
    id: string;
    name: string;
    slug: string;
    role: string;
}

export interface OwnerSession {
    userId: string;
    email: string;
    displayName: string;
    workspace: OwnerWorkspace;
}

interface CsrfTokenResponse {
    headerName: string;
    parameterName: string;
    token: string;
}

interface ApiMessage {
    message: string;
}

interface ApiProblem {
    message?: unknown;
    detail?: unknown;
    title?: unknown;
}

export class ApiError extends Error {
    readonly status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

async function toApiError(
    response: Response,
    fallbackMessage: string
): Promise<ApiError> {
    let message = fallbackMessage;

    try {
        const problem = await response.json() as ApiProblem;

        if (typeof problem.message === "string") {
            message = problem.message;
        } else if (typeof problem.detail === "string") {
            message = problem.detail;
        } else if (typeof problem.title === "string") {
            message = problem.title;
        }
    } catch {
        // Keep the safe fallback when the response has no JSON body.
    }

    return new ApiError(response.status, message);
}

async function readJson<T>(response: Response): Promise<T> {
    if (response.status === 204) {
        return undefined as T;
    }

    return await response.json() as T;
}

async function fetchCsrfToken(): Promise<CsrfTokenResponse> {
    const response = await fetch(`${API_BASE}/public/csrf`, {
        method: "GET",
        credentials: "same-origin",
        cache: "no-store",
        headers: {
            Accept: "application/json"
        }
    });

    if (!response.ok) {
        throw await toApiError(
            response,
            "Unable to prepare a secure request. Please try again."
        );
    }

    return readJson<CsrfTokenResponse>(response);
}

async function post<T>(
    path: string,
    body?: unknown
): Promise<T> {
    const csrf = await fetchCsrfToken();

    const headers: Record<string, string> = {
        Accept: "application/json",
        [csrf.headerName]: csrf.token
    };

    const request: RequestInit = {
        method: "POST",
        credentials: "same-origin",
        cache: "no-store",
        headers
    };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
        request.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE}${path}`, request);

    if (!response.ok) {
        throw await toApiError(
            response,
            "The request could not be completed. Please try again."
        );
    }

    return readJson<T>(response);
}

export async function registerOwner(input: {
    email: string;
    displayName: string;
    password: string;
    businessName: string;
}): Promise<ApiMessage> {
    return post<ApiMessage>("/public/registrations", input);
}

export async function verifyOwnerEmail(
    token: string
): Promise<ApiMessage> {
    return post<ApiMessage>(
        "/public/email-verifications",
        { token }
    );
}

export async function resendOwnerVerification(
    email: string
): Promise<ApiMessage> {
    return post<ApiMessage>(
        "/public/email-verifications/resend",
        { email }
    );
}

export async function loginOwner(input: {
    email: string;
    password: string;
}): Promise<ApiMessage> {
    return post<ApiMessage>("/public/auth/login", input);
}

export async function getOwnerSession(): Promise<OwnerSession> {
    const response = await fetch(`${API_BASE}/owner/me`, {
        method: "GET",
        credentials: "same-origin",
        cache: "no-store",
        headers: {
            Accept: "application/json"
        }
    });

    if (!response.ok) {
        throw await toApiError(
            response,
            "Your owner session could not be verified."
        );
    }

    return readJson<OwnerSession>(response);
}

export async function logoutOwner(): Promise<void> {
    await post<void>("/owner/logout");
}
