import Link from "next/link";

const workflow = [
    {
        number: "01",
        title: "Set the direction",
        description:
            "Give a digital employee a clear objective, the context it needs and the boundaries for the work.",
        symbol: "↗"
    },
    {
        number: "02",
        title: "Follow the work",
        description:
            "Understand what has started, what is progressing and where your attention may be needed.",
        symbol: "◷"
    },
    {
        number: "03",
        title: "Review the outcome",
        description:
            "Inspect structured deliverables, explore their sources and decide what should happen next.",
        symbol: "✓"
    }
];

const principles = [
    {
        number: "01",
        title: "Owner-directed",
        description:
            "You set the objective, decide what matters and retain control of important decisions."
    },
    {
        number: "02",
        title: "Work you can follow",
        description:
            "Assignments and outcomes are designed to be visible rather than hidden behind an AI chat."
    },
    {
        number: "03",
        title: "Built around your business",
        description:
            "Start with useful specialists and grow the digital workforce around the work your business needs."
    }
];

function BrandMark() {
    return (
        <span className="public-brand-mark" aria-hidden="true">
            S
        </span>
    );
}

export default function PublicLandingPage() {
    return (
        <main className="public-site">
            <header className="public-header">
                <div className="public-container public-navigation">
                    <Link
                        href="/"
                        className="public-brand"
                        aria-label="SoloCrew home"
                    >
                        <BrandMark />

                        <span className="public-brand-copy">
                            <strong>SoloCrew</strong>
                            <span>YOUR DIGITAL WORKFORCE</span>
                        </span>
                    </Link>

                    <nav
                        className="public-navigation-links"
                        aria-label="Main navigation"
                    >
                        <a href="#how-it-works">How it works</a>
                        <a href="#principles">Our approach</a>
                    </nav>

                    <div className="public-navigation-actions">
                        <Link
                            href="/login"
                            className="public-login-link"
                        >
                            Sign in
                        </Link>

                        <Link
                            href="/register"
                            className="public-button public-button-small"
                        >
                            Create your workspace
                            <span aria-hidden="true">↗</span>
                        </Link>
                    </div>
                </div>
            </header>

            <section className="public-hero">
                <div className="public-container public-hero-grid">
                    <div className="public-hero-copy">
                        <p className="public-eyebrow">
                            <span />
                            A DIFFERENT WAY TO RUN YOUR BUSINESS
                        </p>

                        <h1>
                            Your business.
                            <span>A workforce of your own.</span>
                        </h1>

                        <p className="public-hero-description">
                            Bring capable digital employees into your
                            business. Give them clear direction, follow
                            their work and stay in control of the
                            decisions that matter.
                        </p>

                        <div className="public-hero-actions">
                            <Link
                                href="/register"
                                className="public-button"
                            >
                                Create your workspace
                                <span aria-hidden="true">↗</span>
                            </Link>

                            <Link
                                href="#how-it-works"
                                className="public-text-link"
                            >
                                See how SoloCrew works
                                <span aria-hidden="true">↓</span>
                            </Link>
                        </div>

                        <div className="public-trust-note">
                            <span className="public-trust-mark">
                                ✓
                            </span>

                            <div>
                                <strong>
                                    Built around owner control.
                                </strong>

                                <p>
                                    Your business records, permissions
                                    and important decisions remain
                                    under your control.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div
                        className="public-hero-visual"
                        aria-label="Illustrative SoloCrew workforce preview"
                    >
                        <div className="public-visual-glow" />

                        <div className="public-preview-window">
                            <div className="public-preview-header">
                                <div className="public-preview-brand">
                                    <BrandMark />
                                    <span>SoloCrew</span>
                                </div>

                                <span className="public-preview-status">
                                    <span />
                                    WORKSPACE PREVIEW
                                </span>
                            </div>

                            <div className="public-preview-body">
                                <div className="public-preview-heading">
                                    <div>
                                        <p>YOUR WORKSPACE</p>
                                        <h2>Meet your workforce.</h2>
                                    </div>

                                    <span className="public-preview-count">
                                        01
                                    </span>
                                </div>

                                <article className="public-specialist-preview">
                                    <div className="public-specialist-avatar">
                                        R
                                    </div>

                                    <div className="public-specialist-copy">
                                        <span>YOUR FIRST SPECIALIST</span>
                                        <strong>
                                            Personal Research Specialist
                                        </strong>
                                        <p>
                                            Research, findings and
                                            traceable source references.
                                        </p>
                                    </div>

                                    <span className="public-specialist-arrow">
                                        ↗
                                    </span>
                                </article>

                                <div className="public-preview-divider" />

                                <div className="public-preview-work">
                                    <div>
                                        <span className="public-preview-work-icon">
                                            01
                                        </span>
                                        <span>Set the direction</span>
                                    </div>

                                    <div>
                                        <span className="public-preview-work-icon">
                                            02
                                        </span>
                                        <span>Follow the work</span>
                                    </div>

                                    <div>
                                        <span className="public-preview-work-icon">
                                            03
                                        </span>
                                        <span>Review the outcome</span>
                                    </div>
                                </div>
                            </div>

                            <div className="public-preview-footer">
                                <span className="public-preview-footer-dot" />
                                Owner-controlled by design
                            </div>
                        </div>

                        <div className="public-floating-note">
                            <span className="public-floating-note-icon">
                                ✳
                            </span>

                            <span>
                                <strong>Work with purpose.</strong>
                                <small>Progress you can follow.</small>
                            </span>
                        </div>
                    </div>
                </div>
            </section>

            <section
                className="public-workflow-section"
                id="how-it-works"
            >
                <div className="public-container">
                    <div className="public-section-heading">
                        <div>
                            <p className="public-section-eyebrow">
                                THE SOLOCREW WORKFLOW
                            </p>

                            <h2>
                                Clear direction.
                                <span>Visible progress. Useful outcomes.</span>
                            </h2>
                        </div>

                        <p>
                            A digital workforce should make running
                            your business clearer, not more complicated.
                        </p>
                    </div>

                    <div className="public-workflow-grid">
                        {workflow.map((item) => (
                            <article
                                className="public-workflow-card"
                                key={item.number}
                            >
                                <div className="public-workflow-card-top">
                                    <span>{item.number}</span>
                                    <span>{item.symbol}</span>
                                </div>

                                <h3>{item.title}</h3>
                                <p>{item.description}</p>
                            </article>
                        ))}
                    </div>
                </div>
            </section>

            <section
                className="public-principles-section"
                id="principles"
            >
                <div className="public-container public-principles-layout">
                    <div className="public-principles-heading">
                        <p className="public-section-eyebrow">
                            A BETTER WAY TO WORK
                        </p>

                        <h2>
                            More capability.
                            <span>Without giving up control.</span>
                        </h2>

                        <p>
                            SoloCrew is being built to put useful
                            digital capability alongside the owner,
                            not between the owner and the business.
                        </p>
                    </div>

                    <div className="public-principles-list">
                        {principles.map((item) => (
                            <article
                                className="public-principle"
                                key={item.number}
                            >
                                <span>{item.number}</span>

                                <div>
                                    <h3>{item.title}</h3>
                                    <p>{item.description}</p>
                                </div>
                            </article>
                        ))}
                    </div>
                </div>
            </section>

            <section className="public-final-cta">
                <div className="public-container public-final-cta-inner">
                    <div>
                        <p className="public-section-eyebrow">
                            MAKE ROOM FOR MEANINGFUL WORK
                        </p>

                        <h2>
                            Start building your
                            <span>digital workforce.</span>
                        </h2>

                        <p>
                            Create your owner workspace and take the
                            first step with SoloCrew.
                        </p>
                    </div>

                    <Link
                        href="/register"
                        className="public-button public-button-light"
                    >
                        Create your workspace
                        <span aria-hidden="true">↗</span>
                    </Link>
                </div>
            </section>

            <footer className="public-footer">
                <div className="public-container public-footer-inner">
                    <Link href="/" className="public-brand">
                        <BrandMark />

                        <span className="public-brand-copy">
                            <strong>SoloCrew</strong>
                            <span>YOUR DIGITAL WORKFORCE</span>
                        </span>
                    </Link>

                    <p>
                        Your business. Your digital workforce.
                    </p>

                    <span>
                        © {new Date().getFullYear()} SoloCrew
                    </span>
                </div>
            </footer>
        </main>
    );
}
