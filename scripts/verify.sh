#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "== pocatcher-api: mvn test =="
(cd "${ROOT}/pocatcher-api" && mvn -B test)

echo "== pocatcher-ui: npm ci, build, lint, typecheck =="
(cd "${ROOT}/pocatcher-ui" && npm ci && npm run build && npm run lint && npm run typecheck && npm run test)

echo "== All checks passed =="
