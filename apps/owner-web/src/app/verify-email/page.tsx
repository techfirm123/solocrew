import { AuthShell } from "@/components/auth/auth-shell";
import { VerificationForm } from "@/components/auth/verification-form";

type PageSearchParams = {
    [key: string]: string | string[] | undefined;
};

export default async function VerifyEmailPage({
    searchParams
}: {
    searchParams: Promise<PageSearchParams>;
}) {
    const params = await searchParams;

    const token = Array.isArray(params.token)
        ? params.token[0] ?? ""
        : params.token ?? "";

    return (
        <AuthShell
            eyebrow="EMAIL VERIFICATION"
            title="Verify your email."
            description="Confirm your email address to activate your SoloCrew owner account."
        >
            <VerificationForm initialToken={token} />
        </AuthShell>
    );
}
