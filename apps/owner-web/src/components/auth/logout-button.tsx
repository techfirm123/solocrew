"use client";

import { useState } from "react";

import { logoutOwner } from "@/lib/api-client";

export function LogoutButton() {
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    async function handleLogout() {
        setSubmitting(true);
        setError("");

        try {
            await logoutOwner();
            window.location.assign("/login?reason=signed-out");
        } catch (cause) {
            setError(
                cause instanceof Error
                    ? cause.message
                    : "Unable to sign out. Please try again."
            );
            setSubmitting(false);
        }
    }

    return (
        <div className="owner-logout">
            <button
                type="button"
                className="owner-logout-button"
                onClick={handleLogout}
                disabled={submitting}
            >
                {submitting ? "Signing out..." : "Sign out"}
            </button>

            {error && (
                <span className="owner-logout-error" role="alert">
                    {error}
                </span>
            )}
        </div>
    );
}
