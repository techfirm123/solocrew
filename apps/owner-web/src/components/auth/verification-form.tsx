"use client";

import {
    useEffect,
    useRef,
    useState,
    type FormEvent
} from "react";
import Link from "next/link";

import {
    resendOwnerVerification,
    verifyOwnerEmail
} from "@/lib/api-client";

export function VerificationForm({
    initialToken = ""
}: {
    initialToken?: string;
}) {
    const [email, setEmail] = useState("");

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [verified, setVerified] = useState(false);

    const [verifying, setVerifying] = useState(false);
    const [resending, setResending] = useState(false);

    // Prevent duplicate verification requests during React Strict Mode
    // effect re-execution.
    const verificationStarted = useRef(false);

    useEffect(() => {
        const token = initialToken.trim();

        if (!token || verificationStarted.current) {
            return;
        }

        verificationStarted.current = true;

        // Remove the token from the visible URL immediately.
        // Preserve Next.js history state and any unrelated query parameters.
        const cleanUrl = new URL(window.location.href);
        cleanUrl.searchParams.delete("token");

        window.history.replaceState(
            window.history.state,
            "",
            `${cleanUrl.pathname}${cleanUrl.search}${cleanUrl.hash}`
        );

        async function verifyFromEmailLink() {
            setError("");
            setMessage("");
            setVerifying(true);

            try {
                const response = await verifyOwnerEmail(token);

                setVerified(true);
                setMessage(response.message);
            } catch (cause) {
                setError(
                    cause instanceof Error
                        ? cause.message
                        : "This verification link is invalid or has expired."
                );
            } finally {
                setVerifying(false);
            }
        }

        void verifyFromEmailLink();
    }, [initialToken]);

    async function handleResend(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setError("");
        setMessage("");

        const normalizedEmail = email.trim();

        if (!normalizedEmail) {
            setError("Enter your registered email address.");
            return;
        }

        setResending(true);

        try {
            const response = await resendOwnerVerification(
                normalizedEmail
            );

            setMessage(response.message);
        } catch (cause) {
            setError(
                cause instanceof Error
                    ? cause.message
                    : "Unable to submit the resend request."
            );
        } finally {
            setResending(false);
        }
    }

    if (verified) {
        return (
            <div className="auth-success-panel" role="status">
                <div className="auth-result-icon">
                    ✓
                </div>

                <h3>Email address verified</h3>

                <p>{message}</p>

                <Link
                    href="/login"
                    className="auth-submit auth-submit-link"
                >
                    Continue to sign in
                </Link>
            </div>
        );
    }

    return (
        <>
            <div className="auth-verification-status" role="status">
                {verifying ? (
                    <>
                        <div className="auth-result-icon">
                            <span className="auth-verification-spinner" />
                        </div>

                        <h3>Verifying your email</h3>

                        <p>
                            We're confirming your email address.
                            This should only take a moment.
                        </p>
                    </>
                ) : initialToken ? (
                    <>
                        <div className="auth-result-icon auth-result-icon-warning">
                            !
                        </div>

                        <h3>Verification could not be completed</h3>

                        <p>
                            Check the message below. You can request
                            another verification email if this link
                            has expired or cannot be used.
                        </p>
                    </>
                ) : (
                    <>
                        <div className="auth-result-icon">
                            ✉
                        </div>

                        <h3>Open your verification email</h3>

                        <p>
                            Select the verification link in the email
                            we sent you. SoloCrew will verify your
                            address automatically.
                        </p>
                    </>
                )}
            </div>

            {error && (
                <div className="auth-error" role="alert">
                    {error}
                </div>
            )}

            {message && (
                <div className="auth-notice" role="status">
                    {message}
                </div>
            )}

            <div className="auth-divider">
                <span>Need another verification email?</span>
            </div>

            <form className="auth-form" onSubmit={handleResend}>
                <div className="auth-field">
                    <label htmlFor="verification-email">
                        Registered email address
                    </label>

                    <input
                        id="verification-email"
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

                <button
                    className="auth-submit auth-submit-secondary"
                    type="submit"
                    disabled={resending || verifying}
                >
                    {resending
                        ? "Submitting..."
                        : "Resend verification email"}
                </button>
            </form>

            <div className="auth-form-footer">
                <span>Already verified?</span>
                <Link href="/login">Sign in</Link>
            </div>
        </>
    );
}
