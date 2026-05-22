import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                source: '/api/:path*',
                destination: 'https://107.23.124.17.nip.io/api/:path*',
            },
        ];
    },
};

export default nextConfig;