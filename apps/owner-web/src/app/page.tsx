import { OwnerWorkspaceShell } from "@/components/owner-workspace-shell";

export default function CommandCenterPage() {
    return (
        <OwnerWorkspaceShell>
            <div className="command-center">
                <section className="hero-section">
                    <div className="hero-copy">
                        <p className="eyebrow">
                            <span className="eyebrow-line" />
                            THE OWNER COMMAND CENTER
                        </p>

                        <h1>
                            Make room for the work
                            <span>that matters.</span>
                        </h1>

                        <p className="hero-description">
                            Bring a capable digital workforce into your
                            business. Give clear direction, stay close to
                            the work, and remain in control of the decisions
                            that matter.
                        </p>

                        <div className="hero-note">
                            <div className="hero-note-number">
                                01
                            </div>

                            <div>
                                <strong>
                                    Your workspace is taking shape.
                                </strong>

                                <p>
                                    We're starting with a Personal Research
                                    Specialist, then expanding the team
                                    around the work your business needs.
                                </p>
                            </div>
                        </div>
                    </div>

                    <article className="specialist-card">
                        <div className="specialist-card-top">
                            <span className="card-overline">
                                FIRST DIGITAL EMPLOYEE
                            </span>

                            <span className="development-status">
                                <span />
                                IN DEVELOPMENT
                            </span>
                        </div>

                        <div
                            className="specialist-visual"
                            aria-hidden="true"
                        >
                            <div className="specialist-orbit specialist-orbit-one" />
                            <div className="specialist-orbit specialist-orbit-two" />

                            <div className="specialist-visual-core">
                                <span>R</span>
                            </div>

                            <span className="specialist-orbit-dot specialist-orbit-dot-one" />
                            <span className="specialist-orbit-dot specialist-orbit-dot-two" />
                            <span className="specialist-orbit-dot specialist-orbit-dot-three" />
                        </div>

                        <div className="specialist-card-content">
                            <p className="card-overline">
                                YOUR FIRST SPECIALIST
                            </p>

                            <h2>
                                Personal Research Specialist
                            </h2>

                            <p className="specialist-description">
                                Turn an open question into structured
                                research, clear findings and traceable
                                source references.
                            </p>

                            <div className="capability-tags">
                                <span>Research briefs</span>
                                <span>Source references</span>
                                <span>Follow-up</span>
                            </div>
                        </div>
                    </article>
                </section>

                <section className="workflow-section">
                    <div className="section-heading">
                        <div>
                            <p className="section-overline">
                                THE SOLOCREW WORKFLOW
                            </p>

                            <h2>
                                Clear direction. Visible progress.
                                Useful outcomes.
                            </h2>
                        </div>

                        <p className="section-description">
                            The Command Center is designed to make
                            delegation understandable and the results
                            accountable.
                        </p>
                    </div>

                    <div className="workflow-cards">
                        <article className="workflow-card">
                            <div className="workflow-card-top">
                                <span className="workflow-number">
                                    01
                                </span>

                                <span className="workflow-symbol">
                                    ↗
                                </span>
                            </div>

                            <h3>Set the direction</h3>

                            <p>
                                Give a specialist a clear objective,
                                the relevant business context and
                                boundaries for the work.
                            </p>
                        </article>

                        <article className="workflow-card">
                            <div className="workflow-card-top">
                                <span className="workflow-number">
                                    02
                                </span>

                                <span className="workflow-symbol">
                                    ◷
                                </span>
                            </div>

                            <h3>Follow the work</h3>

                            <p>
                                Understand what has started, what
                                is progressing and where your
                                attention may be needed.
                            </p>
                        </article>

                        <article className="workflow-card">
                            <div className="workflow-card-top">
                                <span className="workflow-number">
                                    03
                                </span>

                                <span className="workflow-symbol">
                                    ✓
                                </span>
                            </div>

                            <h3>Review the outcome</h3>

                            <p>
                                Read structured deliverables,
                                inspect their sources and decide
                                what should happen next.
                            </p>
                        </article>
                    </div>
                </section>

                <section className="activity-panel">
                    <div className="activity-panel-heading">
                        <div>
                            <p className="section-overline">
                                YOUR WORKSTREAM
                            </p>

                            <h2>Recent activity</h2>

                            <p className="activity-description">
                                Assignments, progress and completed
                                work will be collected here.
                            </p>
                        </div>

                        <span className="activity-status">
                            Awaiting first specialist
                        </span>
                    </div>

                    <div className="activity-empty-state">
                        <div className="activity-empty-icon" aria-hidden="true">
                            <span className="empty-icon-page" />
                            <span className="empty-icon-line empty-icon-line-one" />
                            <span className="empty-icon-line empty-icon-line-two" />
                            <span className="empty-icon-line empty-icon-line-three" />
                        </div>

                        <h3>
                            Your workstream starts with an assignment.
                        </h3>

                        <p>
                            Once the Personal Research Specialist is
                            connected, your delegated work, progress
                            and completed reports will appear here.
                        </p>
                    </div>
                </section>

                <section className="control-principle">
                    <div className="control-principle-mark" aria-hidden="true">
                        <span>✓</span>
                    </div>

                    <div>
                        <strong>
                            You remain in control.
                        </strong>

                        <p>
                            SoloCrew is being built around clear
                            responsibilities, visible work and
                            owner-controlled decisions.
                        </p>
                    </div>
                </section>
            </div>
        </OwnerWorkspaceShell>
    );
}
