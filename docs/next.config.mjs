import { createMDX } from 'fumadocs-mdx/next';

const withMDX = createMDX();

/** @type {import('next').NextConfig} */
const config = {
  reactStrictMode: true,
  basePath: '/compose-camera',
  ...(process.env.NODE_ENV === 'production'
    ? {
        output: 'export',
      }
    : {}),
  images: {
    unoptimized: true,
  },
};

export default withMDX(config);
