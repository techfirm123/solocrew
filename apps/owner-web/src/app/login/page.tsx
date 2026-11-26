import Link from "next/link";

import { AuthShell } from "@/components/auth/auth-shell";
import { LoginForm } from "@/components/auth/login-form";

type PageSearchParams = {
    [key: string]: string | string[] | undefined;
};

export default async function LoginPage({
    searchParams
}: {
    searchParams: Promise<PageSearchParams>;
}) {
    const params = await searchParams;
    const reason = Array.isArray(params.reason)
        ? params.reason[0]
        : params.reason;

    let notice: string | undefined;

    if (reason === "session-expired") {
        notice = "Your session has expired. Please sign in again.";
    } else if (reason === "signed-out") {
        notice = "You have signed out successfully.";
    } else if (reason === "workspace-unavailable") {
        notice =
            "Your account does not currently have access to an active business workspace.";
    }

    return (
        <AuthShell
            eyebrow="OWNER ACCESS"
            title="Welcome back."
            description="Sign in to continue to your business workspace."
        >
            <LoginForm notice={notice} />

            <p className="auth-legal-note">
                Need help with email verification?{" "}
                <Link href="/verify-email">
                    Open verification
                </Link>
            </p>
        </AuthShell>
    );
}
