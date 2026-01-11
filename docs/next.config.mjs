import { createMDX } from 'fumadocs-mdx/next';

const withMDX = createMDX();

/** @type {import('next').NextConfig} */
const config = {
  reactStrictMode: true,
  ...(process.env.NODE_ENV === 'production'
    ? {
        output: 'export',
        basePath: '/compose-camera',
      }
    : {}),
  images: {
    unoptimized: true,
  },
};

export default withMDX(config);
