import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Docker 런타임 이미지 최소화를 위한 독립 실행형 출력(.next/standalone).
  output: "standalone",
};

export default nextConfig;
