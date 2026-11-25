#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

failed=0

while IFS= read -r -d '' file; do
  # Ignore binary files; grep -I keeps this check safe for images and other assets.
  if grep -Iq . "$file"; then
    if grep -nE '[[:blank:]]+$' "$file"; then
      echo "Trailing whitespace found in ${file}" >&2
      failed=1
    fi
  fi
done < <(find . -type f \
  -not -path './.git/*' \
  -not -path './node_modules/*' \
  -not -path './.pnpm-store/*' \
  -not -path './.gradle/*' \
  -not -path '*/build/*' \
  -print0)

if [[ "$failed" -ne 0 ]]; then
  exit 1
fi

echo "Whitespace check passed."
