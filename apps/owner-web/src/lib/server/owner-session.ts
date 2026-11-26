import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import type { OwnerSession } from "@/lib/api-client";

const BACKEND_URL = (
    process.env.SOLOCREW_BACKEND_URL ?? "http://127.0.0.1:8080"
).replace(/\/+$/, "");

const SESSION_COOKIE_NAME = "SOLOCREW_SESSION";

export async function requireOwnerSession(): Promise<OwnerSession> {
    const cookieStore = await cookies();
    const sessionCookie = cookieStore.get(SESSION_COOKIE_NAME);

    if (!sessionCookie?.value) {
        redirect("/login?reason=session-expired");
    }

    let response: Response;

    try {
        response = await fetch(
            `${BACKEND_URL}/api/v1/owner/me`,
            {
                method: "GET",
                headers: {
                    Accept: "application/json",
                    Cookie: `${SESSION_COOKIE_NAME}=${sessionCookie.value}`
                },
                cache: "no-store"
            }
        );
    } catch {
        throw new Error(
            "Unable to verify your SoloCrew session. " +
            "Check that the backend is running and try again."
        );
    }

    if (response.status === 401) {
        redirect("/login?reason=session-expired");
    }

    if (response.status === 403) {
        redirect("/login?reason=workspace-unavailable");
    }

    if (!response.ok) {
        throw new Error(
            "Unable to load your owner workspace. Please try again."
        );
    }

    return await response.json() as OwnerSession;
}
