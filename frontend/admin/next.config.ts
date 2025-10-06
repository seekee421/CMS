import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  /* config options here */
  turbopack: {
    // 显式指定 Turbopack 根目录，避免多锁文件导致的误判
    root: path.resolve(__dirname),
  },
};

export default nextConfig;
