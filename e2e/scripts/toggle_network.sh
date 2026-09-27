#!/usr/bin/env bash
# FASE 74.1 helper: flip wifi + mobile data for the "offline resilience"
# scenario (CbtOfflineResilienceE2ETest). Mirrors
# AppiumTestBase.setNetworkEnabled() for use outside the JVM test process
# (e.g. manual reproduction, or a CI step log for humans reading the run).
#
# Usage: toggle_network.sh <on|off> [device_udid]
set -euo pipefail

STATE="${1:?Usage: toggle_network.sh <on|off> [device_udid]}"
UDID="${2:-}"

ADB_ARGS=()
if [[ -n "$UDID" ]]; then
  ADB_ARGS+=(-s "$UDID")
fi

case "$STATE" in
  on)
    adb "${ADB_ARGS[@]}" shell svc wifi enable
    adb "${ADB_ARGS[@]}" shell svc data enable
    echo "Network re-enabled."
    ;;
  off)
    adb "${ADB_ARGS[@]}" shell svc wifi disable
    adb "${ADB_ARGS[@]}" shell svc data disable
    echo "Network disabled — device is now offline."
    ;;
  *)
    echo "Unknown state '$STATE' — expected 'on' or 'off'." >&2
    exit 1
    ;;
esac
