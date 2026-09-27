#!/usr/bin/env bash
# FASE 74.3: Monkey stress test — rapidly fuzzes ~20 screens' worth of taps/
# swipes to shake out memory leaks, ANRs and jank that only show up under
# real navigation churn, not a scripted single-path E2E run.
#
# Usage: monkey_stress_test.sh <package> [event_count] [device_udid]
set -euo pipefail

PACKAGE="${1:?Usage: monkey_stress_test.sh <package> [event_count] [device_udid]}"
# ~20 screens at a handful of interactions each; tune per how deep the nav
# graph is. This is intentionally an approximation, not a literal "20
# screens" counter — Monkey has no concept of "screen", only raw events.
EVENT_COUNT="${2:-800}"
UDID="${3:-}"

ADB_ARGS=()
if [[ -n "$UDID" ]]; then
  ADB_ARGS+=(-s "$UDID")
fi

echo "Clearing logcat before the run so ANR/crash grep below is clean..."
adb "${ADB_ARGS[@]}" logcat -c

echo "Running monkey against $PACKAGE ($EVENT_COUNT events)..."
# --pct-touch/--pct-motion bias toward realistic tap/scroll navigation over
# --pct-trackball or --pct-nav, which don't reflect how this app is actually
# used. --throttle adds a small delay between events so it stresses
# navigation, not just raw event-queue throughput.
set +e
adb "${ADB_ARGS[@]}" shell monkey -p "$PACKAGE" \
  --pct-touch 40 --pct-motion 30 --pct-appswitch 10 \
  --throttle 100 -v "$EVENT_COUNT"
MONKEY_EXIT=$?
set -e

if [[ $MONKEY_EXIT -ne 0 ]]; then
  echo "Monkey exited non-zero ($MONKEY_EXIT) — it stops early on an uncaught"
  echo "exception or ANR by default, which is itself already a failure."
  exit 1
fi

echo "Checking logcat for crashes/ANRs raised during the run..."
if adb "${ADB_ARGS[@]}" logcat -d | grep -E "FATAL EXCEPTION|ANR in $PACKAGE"; then
  echo "Crash or ANR detected during monkey stress run." >&2
  exit 1
fi

echo "Monkey stress run completed with no crash/ANR in logcat."
