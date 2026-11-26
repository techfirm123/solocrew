import Link from "next/link";
import type { ReactNode } from "react";

import type { OwnerSession } from "@/lib/api-client";
import { LogoutButton } from "@/components/auth/logout-button";

type NavigationIconName =
    | "command"
    | "workforce"
    | "work"
    | "business"
    | "intelligence";

function NavigationIcon({
    name
}: {
    name: NavigationIconName;
}) {
    switch (name) {
        case "command":
            return (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <rect x="3.75" y="3.75" width="6.25" height="6.25" rx="1.4" />
                    <rect x="14" y="3.75" width="6.25" height="6.25" rx="1.4" />
                    <rect x="3.75" y="14" width="6.25" height="6.25" rx="1.4" />
                    <rect x="14" y="14" width="6.25" height="6.25" rx="1.4" />
                </svg>
            );

        case "workforce":
            return (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <circle cx="9" cy="8" r="3" />
                    <path d="M3.8 19.2c.5-3.1 2.3-4.7 5.2-4.7s4.7 1.6 5.2 4.7" />
                    <path d="M16 5.5a2.8 2.8 0 0 1 0 5.4" />
                    <path d="M17 14.7c2 .4 3.1 1.9 3.3 4.5" />
                </svg>
            );

        case "work":
            return (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <rect x="3.5" y="8" width="17" height="11.5" rx="2" />
                    <path d="M8 8V6.5A1.5 1.5 0 0 1 9.5 5h5A1.5 1.5 0 0 1 16 6.5V8" />
                    <path d="M3.5 12.5h17" />
                    <path d="M10 12.5v1.8h4v-1.8" />
                </svg>
            );

        case "business":
            return (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M4 20V5.7L12 3l8 2.7V20" />
                    <path d="M8 20v-5h8v5" />
                    <path d="M8 8.5h.01M16 8.5h.01M8 12h.01M16 12h.01" />
                    <path d="M3 20h18" />
                </svg>
            );

        case "intelligence":
            return (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M4 19.5V13" />
                    <path d="M9.5 19.5V8.5" />
                    <path d="M15 19.5V11" />
                    <path d="M20 19.5V4.5" />
                    <path d="M3 20h18" />
                </svg>
            );
    }
}

const upcomingNavigation: {
    label: string;
    icon: NavigationIconName;
}[] = [
    {
        label: "Workforce",
        icon: "workforce"
    },
    {
        label: "Work Hub",
        icon: "work"
    },
    {
        label: "Business",
        icon: "business"
    },
    {
        label: "Intelligence",
        icon: "intelligence"
    }
];

function initials(displayName: string): string {
    const parts = displayName
        .trim()
        .split(/\s+/)
        .filter(Boolean);

    if (parts.length === 0) {
        return "SC";
    }

    return parts
        .slice(0, 2)
        .map((part) => part.charAt(0).toUpperCase())
        .join("");
}

export function OwnerWorkspaceShell({
    owner,
    children
}: {
    owner: OwnerSession;
    children: ReactNode;
}) {
    return (
        <div className="workspace-shell">
            <aside className="sidebar">
                <Link
                    href="/workspace"
                    className="brand"
                    aria-label="SoloCrew Command Center"
                >
                    <span className="brand-mark" aria-hidden="true">
                        <span>S</span>
                    </span>

                    <span className="brand-copy">
                        <strong>SoloCrew</strong>
                        <span>OWNER WORKSPACE</span>
                    </span>
                </Link>

                <div className="sidebar-divider" />

                <nav
                    className="sidebar-navigation"
                    aria-label="Owner workspace navigation"
                >
                    <p className="sidebar-section-label">
                        YOUR WORKSPACE
                    </p>

                    <Link
                        href="/"
                        className="navigation-item navigation-item-active"
                        aria-current="page"
                    >
                        <span className="navigation-icon">
                            <NavigationIcon name="command" />
                        </span>

                        <span className="navigation-label">
                            Command Center
                        </span>
                    </Link>

                    <p className="sidebar-section-label sidebar-section-spaced">
                        YOUR BUSINESS
                    </p>

                    {upcomingNavigation.map((item) => (
                        <div
                            key={item.label}
                            className="navigation-item navigation-item-disabled"
                            aria-disabled="true"
                            title={`${item.label} will become available as its capabilities are built.`}
                        >
                            <span className="navigation-icon">
                                <NavigationIcon name={item.icon} />
                            </span>

                            <span className="navigation-label">
                                {item.label}
                            </span>

                            <span className="navigation-state">
                                Planned
                            </span>
                        </div>
                    ))}
                </nav>

                <div className="sidebar-bottom">
                    <div className="sidebar-bottom-mark" aria-hidden="true">
                        SC
                    </div>

                    <div className="sidebar-bottom-copy">
                        <strong>Owner workspace</strong>
                        <span>{owner.workspace.name}</span>
                    </div>

                    <span className="sidebar-bottom-indicator" />
                </div>
            </aside>

            <div className="workspace-main">
                <header className="topbar">
                    <div className="topbar-breadcrumb">
                        <span>Workspace</span>
                        <span className="breadcrumb-separator">/</span>
                        <strong>Command Center</strong>
                    </div>

                    <div className="topbar-actions">
                        <span
                            className="workspace-status"
                            title={owner.workspace.name}
                        >
                            <span className="workspace-status-dot" />
                            {owner.workspace.name}
                        </span>

                        <div className="owner-profile">
                            <div className="owner-profile-copy">
                                <strong>{owner.displayName}</strong>
                                <span>{owner.email}</span>
                            </div>

                            <div
                                className="owner-avatar"
                                aria-label={owner.displayName}
                                title={owner.displayName}
                            >
                                {initials(owner.displayName)}
                            </div>
                        </div>

                        <LogoutButton />
                    </div>
                </header>

                <main className="workspace-content">
                    {children}
                </main>

                <footer className="workspace-footer">
                    <span>
                        SoloCrew <span className="footer-dot">·</span>
                        Your business. Your digital workforce.
                    </span>

                    <span>
                        {owner.workspace.role} workspace
                    </span>
                </footer>
            </div>
        </div>
    );
}
