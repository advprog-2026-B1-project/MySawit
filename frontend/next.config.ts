import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                source: '/api/:path*',
                destination: 'https://107.23.124.17.nip.io/api/:path*',
            },
            {
                source: '/oauth2/:path*',
                destination: 'https://107.23.124.17.nip.io/oauth2/:path*',
            },
            {
                source: '/login/oauth2/:path*',
                destination: 'https://107.23.124.17.nip.io/login/oauth2/:path*',
            },
        ];
    },
};

export default nextConfig;