import Link from "next/link";
import type { ReactNode } from "react";

export function AuthShell({
    eyebrow,
    title,
    description,
    children
}: {
    eyebrow: string;
    title: string;
    description: string;
    children: ReactNode;
}) {
    return (
        <div className="auth-page">
            <header className="auth-header">
                <Link
                    href="/"
                    className="auth-brand"
                    aria-label="SoloCrew"
                >
                    <span className="auth-brand-mark">S</span>

                    <span className="auth-brand-copy">
                        <strong>SoloCrew</strong>
                        <small>OWNER WORKSPACE</small>
                    </span>
                </Link>

                <span className="auth-header-note">
                    Your business. Your digital workforce.
                </span>
            </header>

            <main className="auth-main">
                <section className="auth-intro">
                    <p className="auth-intro-eyebrow">
                        A DIFFERENT WAY TO RUN YOUR BUSINESS
                    </p>

                    <h1>
                        More room for
                        <span>meaningful work.</span>
                    </h1>

                    <p>
                        Build a capable digital workforce around your
                        business. Delegate with clarity, follow the work
                        and stay in control of important decisions.
                    </p>

                    <div className="auth-principle">
                        <span className="auth-principle-mark">✓</span>

                        <div>
                            <strong>Owner controlled by design</strong>
                            <p>
                                Your business records, permissions and
                                decisions remain under your control.
                            </p>
                        </div>
                    </div>
                </section>

                <section className="auth-card">
                    <div className="auth-card-heading">
                        <p className="auth-card-eyebrow">{eyebrow}</p>
                        <h2>{title}</h2>
                        <p>{description}</p>
                    </div>

                    {children}
                </section>
            </main>

            <footer className="auth-footer">
                <span>SoloCrew</span>
                <span>Secure owner access</span>
            </footer>
        </div>
    );
}
