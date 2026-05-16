#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-fast}"
if [[ $# -gt 0 ]]; then
  shift
fi

usage() {
  cat <<'USAGE'
Usage: scripts/android-local-check.sh [fast|maestro|all] [maestro-target] [-- maestro-args...]

Modes:
  fast     Run the same fast checks as GitHub Actions.
  maestro  Build, install, and run Maestro flows locally.
  all      Run fast checks, then Maestro flows locally.

Examples:
  scripts/android-local-check.sh
  scripts/android-local-check.sh all
  scripts/android-local-check.sh maestro maestro/flows/home-smoke.yaml
  scripts/android-local-check.sh maestro maestro/flows -- --include-tags smoke
USAGE
}

run_fast_checks() {
  ./gradlew testDebugUnitTest lintDebug assembleDebug --continue
}

run_maestro_checks() {
  local target="${1:-maestro/flows}"
  if [[ $# -gt 0 ]]; then
    shift
  fi

  scripts/android-maestro-run.sh "${target}" "$@"
}

case "${MODE}" in
  fast)
    run_fast_checks
    ;;
  maestro)
    run_maestro_checks "$@"
    ;;
  all)
    run_fast_checks
    run_maestro_checks "$@"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac
