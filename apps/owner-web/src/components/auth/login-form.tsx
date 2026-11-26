"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";

import {
    getOwnerSession,
    loginOwner
} from "@/lib/api-client";

export function LoginForm({
    notice
}: {
    notice?: string;
}) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setError("");
        setSubmitting(true);

        try {
            await loginOwner({
                email: email.trim(),
                password
            });

            await getOwnerSession();

            window.location.assign("/workspace");
        } catch (cause) {
            setError(
                cause instanceof Error
                    ? cause.message
                    : "Unable to sign in. Please try again."
            );

            setSubmitting(false);
        }
    }

    return (
        <>
            {notice && (
                <div className="auth-notice" role="status">
                    {notice}
                </div>
            )}

            <form className="auth-form" onSubmit={handleSubmit}>
                <div className="auth-field">
                    <label htmlFor="login-email">Email address</label>

                    <input
                        id="login-email"
                        type="email"
                        name="email"
                        autoComplete="email"
                        autoCapitalize="none"
                        spellCheck={false}
                        placeholder="you@business.com"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        required
                        maxLength={320}
                    />
                </div>

                <div className="auth-field">
                    <label htmlFor="login-password">Password</label>

                    <input
                        id="login-password"
                        type="password"
                        name="password"
                        autoComplete="current-password"
                        placeholder="Enter your password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        required
                        maxLength={72}
                    />
                </div>

                {error && (
                    <div className="auth-error" role="alert">
                        {error}
                    </div>
                )}

                <button
                    className="auth-submit"
                    type="submit"
                    disabled={submitting}
                >
                    {submitting ? "Signing in..." : "Sign in"}
                </button>
            </form>

            <div className="auth-form-footer">
                <span>New to SoloCrew?</span>
                <Link href="/register">Create an owner account</Link>
            </div>
        </>
    );
}
