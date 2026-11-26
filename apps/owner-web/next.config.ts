import type { NextConfig } from "next";

const backendUrl = (
    process.env.SOLOCREW_BACKEND_URL ?? "http://127.0.0.1:8080"
).replace(/\/+$/, "");

const nextConfig: NextConfig = {
    poweredByHeader: false,
    reactStrictMode: true,

    async rewrites() {
        return [
            {
                source: "/api/backend/:path*",
                destination: `${backendUrl}/api/v1/:path*`
            }
        ];
    }
};

export default nextConfig;
