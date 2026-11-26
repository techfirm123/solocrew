import type { Metadata, Viewport } from "next";

import "./globals.css";
import "./auth.css";
import "./brand.css";
import "./design-tokens.css";

export const metadata: Metadata = {
    applicationName: "SoloCrew",
    title: {
        default: "SoloCrew — Your business. Your digital workforce.",
        template: "%s | SoloCrew"
    },
    description:
        "Build a capable digital workforce around your business. Delegate with clarity, follow the work and stay in control of important decisions.",
    robots: {
        index: true,
        follow: true
    }
};

export const viewport: Viewport = {
    width: "device-width",
    initialScale: 1,
    themeColor: "#172d28"
};

export default function RootLayout({
    children
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en" data-theme="midnight-botanical">
            <body>{children}</body>
        </html>
    );
}
