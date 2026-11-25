#!/usr/bin/env bash
set -euo pipefail

failures=0

require_command() {
  local command_name="$1"

  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "MISSING  $command_name"
    failures=$((failures + 1))
    return 1
  fi

  return 0
}

check_major() {
  local label="$1"
  local actual="$2"
  local expected="$3"

  if [[ "$actual" == "$expected" ]]; then
    echo "OK       ${label} ${actual}"
  else
    echo "MISMATCH ${label}: expected major ${expected}, found ${actual:-unknown}"
    failures=$((failures + 1))
  fi
}

if require_command java; then
  java_major="$(java -XshowSettings:properties -version 2>&1 |
    awk -F'= ' '/java.specification.version/ { print $2; exit }')"

  check_major "Java" "$java_major" "21"
fi

if require_command node; then
  node_major="$(node -p 'process.versions.node.split(".")[0]')"
  check_major "Node.js" "$node_major" "24"
fi

if require_command pnpm; then
  pnpm_version="$(pnpm --version)"

  if [[ "$pnpm_version" == "11.29.0" ]]; then
    echo "OK       pnpm ${pnpm_version}"
  else
    echo "MISMATCH pnpm: expected 11.29.0, found ${pnpm_version}"
    failures=$((failures + 1))
  fi
fi

if require_command git; then
  echo "OK       git $(git --version | awk '{ print $3 }')"
fi

if [[ "$failures" -gt 0 ]]; then
  echo
  echo "Toolchain check failed with ${failures} issue(s)."
  echo "See docs/development/local-setup.md."
  exit 1
fi

echo
echo "Toolchain check passed."
