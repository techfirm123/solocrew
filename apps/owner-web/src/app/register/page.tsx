import { AuthShell } from "@/components/auth/auth-shell";
import { RegisterForm } from "@/components/auth/register-form";

export default function RegisterPage() {
    return (
        <AuthShell
            eyebrow="CREATE YOUR WORKSPACE"
            title="Start with your business."
            description="Create your owner account. We'll verify your email before enabling access."
        >
            <RegisterForm />
        </AuthShell>
    );
}
