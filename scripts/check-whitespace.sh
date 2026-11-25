
#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

failed=0

# Check tracked files and non-ignored untracked files.
# Git automatically excludes generated and ignored directories such as .next.
while IFS= read -r -d '' file; do
  # Ignore binary files.
  if [[ -f "$file" ]] && grep -Iq . -- "$file"; then
    if grep -nE '[[:blank:]]+$' -- "$file"; then
      printf 'Trailing whitespace found in %s\n' "$file" >&2
      failed=1
    fi
  fi
done < <(git ls-files --cached --others --exclude-standard -z)

if [[ "$failed" -ne 0 ]]; then
  exit 1
fi

echo "Whitespace check passed."
