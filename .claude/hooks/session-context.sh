#!/usr/bin/env bash
# SessionStart 훅: 세션 시작 시 뚝딱밥 작업 컨텍스트를 stdout으로 출력해 context에 주입한다.
# - 설계 문서(프로젝트-개요)는 경로만 알려주고 필요할 때 Read (토큰 절약)
# - 최신 세션로그는 전체를 주입 (그 안의 "다음에 할 것"으로 바로 이어서 작업)
set -euo pipefail

VAULT="/Users/kimgy/Documents/ObsidianVault/프로젝트/뚝딱밥"

echo "## 뚝딱밥 세션 컨텍스트"
echo ""
echo "- 설계 문서(필요 시 Read): \`$VAULT/프로젝트-개요.md\`"
echo "- 작업이 끝나면 \`session-log\` 스킬로 세션로그를 남긴다."
echo ""

LATEST="$(ls -1 "$VAULT/세션로그"/*.md 2>/dev/null | sort | tail -1 || true)"
if [ -n "$LATEST" ]; then
  echo "### 최신 세션로그: $(basename "$LATEST")"
  echo ""
  cat "$LATEST"
else
  echo "(세션로그 없음)"
fi
