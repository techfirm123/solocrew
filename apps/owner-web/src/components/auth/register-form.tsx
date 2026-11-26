"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";

import { registerOwner } from "@/lib/api-client";

export function RegisterForm() {
    const [displayName, setDisplayName] = useState("");
    const [businessName, setBusinessName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [error, setError] = useState("");
    const [accepted, setAccepted] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setError("");

        if (password !== confirmPassword) {
            setError("The passwords do not match.");
            return;
        }

        if (password.length < 12) {
            setError("Use a password with at least 12 characters.");
            return;
        }

        if (new TextEncoder().encode(password).length > 72) {
            setError(
                "Your password is too long. Use no more than 72 UTF-8 bytes."
            );
            return;
        }

        setSubmitting(true);

        try {
            await registerOwner({
                email: email.trim(),
                displayName: displayName.trim(),
                password,
                businessName: businessName.trim()
            });

            setAccepted(true);
        } catch (cause) {
            setError(
                cause instanceof Error
                    ? cause.message
                    : "Unable to submit registration. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    }

    if (accepted) {
        return (
            <div className="auth-success-panel" role="status">
                <div className="auth-result-icon">
                    ✓
                </div>

                <h3>Registration request received</h3>

                <p>
                    If registration can proceed, SoloCrew will send
                    a verification email to the address provided.
                    Check your inbox and open the verification link.
                </p>

                <p>
                    Your email address will be verified automatically
                    when you open the link. You won't need to copy
                    or enter a token.
                </p>

                <Link
                    href="/verify-email"
                    className="auth-submit auth-submit-link"
                >
                    Continue to email verification
                </Link>

                <div className="auth-form-footer">
                    <span>Already have an account?</span>
                    <Link href="/login">Sign in</Link>
                </div>
            </div>
        );
    }

    return (
        <>
            <form className="auth-form" onSubmit={handleSubmit}>
                <div className="auth-field">
                    <label htmlFor="register-name">
                        Your name
                    </label>

                    <input
                        id="register-name"
                        type="text"
                        name="displayName"
                        autoComplete="name"
                        placeholder="Your full name"
                        value={displayName}
                        onChange={(event) =>
                            setDisplayName(event.target.value)
                        }
                        required
                        minLength={2}
                        maxLength={120}
                    />
                </div>

                <div className="auth-field">
                    <label htmlFor="register-business">
                        Business name
                    </label>

                    <input
                        id="register-business"
                        type="text"
                        name="businessName"
                        autoComplete="organization"
                        placeholder="Your business"
                        value={businessName}
                        onChange={(event) =>
                            setBusinessName(event.target.value)
                        }
                        required
                        minLength={2}
                        maxLength={160}
                    />
                </div>

                <div className="auth-field">
                    <label htmlFor="register-email">
                        Email address
                    </label>

                    <input
                        id="register-email"
                        type="email"
                        name="email"
                        autoComplete="email"
                        autoCapitalize="none"
                        spellCheck={false}
                        placeholder="you@business.com"
                        value={email}
                        onChange={(event) =>
                            setEmail(event.target.value)
                        }
                        required
                        maxLength={320}
                    />
                </div>

                <div className="auth-field">
                    <label htmlFor="register-password">
                        Password
                    </label>

                    <input
                        id="register-password"
                        type="password"
                        name="password"
                        autoComplete="new-password"
                        placeholder="At least 12 characters"
                        value={password}
                        onChange={(event) =>
                            setPassword(event.target.value)
                        }
                        required
                        minLength={12}
                        maxLength={72}
                    />

                    <small>
                        Use at least 12 characters. Maximum 72 UTF-8 bytes.
                    </small>
                </div>

                <div className="auth-field">
                    <label htmlFor="register-confirm-password">
                        Confirm password
                    </label>

                    <input
                        id="register-confirm-password"
                        type="password"
                        name="confirmPassword"
                        autoComplete="new-password"
                        placeholder="Enter the password again"
                        value={confirmPassword}
                        onChange={(event) =>
                            setConfirmPassword(event.target.value)
                        }
                        required
                        minLength={12}
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
                    {submitting
                        ? "Submitting..."
                        : "Create owner account"}
                </button>
            </form>

            <div className="auth-form-footer">
                <span>Already registered?</span>
                <Link href="/login">Sign in</Link>
            </div>
        </>
    );
}
