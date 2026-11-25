import type { Metadata, Viewport } from "next";

import "./globals.css";

export const metadata: Metadata = {
    applicationName: "SoloCrew",
    title: {
        default: "Command Center | SoloCrew",
        template: "%s | SoloCrew"
    },
    description:
        "The SoloCrew Owner Command Center. Coordinate your digital workforce, delegate work and stay in control of business decisions.",
    robots: {
        index: false,
        follow: false
    }
};

export const viewport: Viewport = {
    width: "device-width",
    initialScale: 1,
    themeColor: "#172f29"
};

export default function RootLayout({
    children
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
            <body>{children}</body>
        </html>
    );
}
