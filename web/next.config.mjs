/** @type {import('next').NextConfig} */
const nextConfig = {
  // The browser only ever talks to http://localhost:3000.
  // Next.js forwards /api/* to Spring Boot, so cookies and CSRF work without CORS.
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.API_URL || 'http://localhost:8080'}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
