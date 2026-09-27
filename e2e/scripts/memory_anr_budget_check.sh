#!/usr/bin/env bash
# FASE 74.3: enforces the roadmap's stated budget after a monkey_stress_test.sh
# run — run this immediately after that script while the app process is
# still warm/whatever state the stress run left it in:
#   - RAM (PSS) <= 120MB
#   - 0 ANR events in logcat since the last `logcat -c`
#   - 0 frames over 16.6ms (60Hz budget) in the app's own gfxinfo history
#
# Usage: memory_anr_budget_check.sh <package> [device_udid]
set -euo pipefail

PACKAGE="${1:?Usage: memory_anr_budget_check.sh <package> [device_udid]}"
UDID="${2:-}"
RAM_BUDGET_KB=$((120 * 1024))
FRAME_BUDGET_NS=$((16600000)) # 16.6ms in nanoseconds

ADB_ARGS=()
if [[ -n "$UDID" ]]; then
  ADB_ARGS+=(-s "$UDID")
fi

FAILED=0

echo "== Memory budget (target: <= 120MB PSS) =="
MEMINFO=$(adb "${ADB_ARGS[@]}" shell dumpsys meminfo "$PACKAGE")
echo "$MEMINFO" | grep -A1 "^App Summary" || true
TOTAL_PSS_KB=$(echo "$MEMINFO" | grep "TOTAL PSS:" | head -1 | awk '{print $3}')
if [[ -z "${TOTAL_PSS_KB:-}" ]]; then
  # Older/newer Android versions format this line slightly differently —
  # fall back to the legacy "TOTAL:" row rather than silently reporting a
  # pass on a value we never actually parsed.
  TOTAL_PSS_KB=$(echo "$MEMINFO" | grep -E "^\s*TOTAL\s+" | head -1 | awk '{print $2}')
fi

if [[ -z "${TOTAL_PSS_KB:-}" ]]; then
  echo "Could not parse TOTAL PSS from 'dumpsys meminfo $PACKAGE' output — the"
  echo "field name/position varies by Android version. Inspect the raw output"
  echo "above and adjust the awk column before trusting this check."
  FAILED=1
elif [[ "$TOTAL_PSS_KB" -gt "$RAM_BUDGET_KB" ]]; then
  echo "FAIL: TOTAL PSS ${TOTAL_PSS_KB}KB exceeds budget ${RAM_BUDGET_KB}KB (120MB)"
  FAILED=1
else
  echo "OK: TOTAL PSS ${TOTAL_PSS_KB}KB is within the 120MB budget"
fi

echo
echo "== ANR budget (target: 0 events since last logcat -c) =="
if adb "${ADB_ARGS[@]}" logcat -d | grep -q "ANR in $PACKAGE"; then
  echo "FAIL: at least one ANR was logged for $PACKAGE"
  FAILED=1
else
  echo "OK: no ANR logged for $PACKAGE"
fi

echo
echo "== Dropped-frame budget (target: 0 frames over 16.6ms @ 60Hz) =="
# NOTE: `dumpsys gfxinfo <pkg> framestats` column layout has changed across
# Android versions (Android 6 vs 10 vs 14 report different sets of timing
# columns). This computes frame duration as FRAME_COMPLETED - INTENDED_VSYNC
# using the header row gfxinfo itself prints, instead of a hardcoded column
# index, so it stays correct across versions that still expose both names —
# verify against your target API level's actual header if this ever reports
# zero rows parsed.
FRAMESTATS=$(adb "${ADB_ARGS[@]}" shell dumpsys gfxinfo "$PACKAGE" framestats)
HEADER_LINE=$(echo "$FRAMESTATS" | grep -m1 "INTENDED_VSYNC")
if [[ -z "$HEADER_LINE" ]]; then
  echo "Could not find a framestats header — is the app in the foreground"
  echo "with at least one rendered frame since it was last profiled?"
  FAILED=1
else
  VSYNC_COL=$(echo "$HEADER_LINE" | tr ',' '\n' | grep -n "^INTENDED_VSYNC$" | head -1 | cut -d: -f1)
  COMPLETE_COL=$(echo "$HEADER_LINE" | tr ',' '\n' | grep -n "^FRAME_COMPLETED$" | head -1 | cut -d: -f1)

  if [[ -z "$VSYNC_COL" || -z "$COMPLETE_COL" ]]; then
    echo "Header found but missing expected column names — dumping raw header for manual review:"
    echo "$HEADER_LINE"
    FAILED=1
  else
    DROPPED=$(echo "$FRAMESTATS" | grep -E "^[0-9]+," | awk -F',' -v v="$VSYNC_COL" -v c="$COMPLETE_COL" -v budget="$FRAME_BUDGET_NS" \
      '{ dur = $c - $v; if (dur > budget) count++ } END { print count+0 }')
    if [[ "$DROPPED" -gt 0 ]]; then
      echo "FAIL: $DROPPED frame(s) exceeded the 16.6ms budget"
      FAILED=1
    else
      echo "OK: 0 frames exceeded the 16.6ms budget"
    fi
  fi
fi

echo
if [[ "$FAILED" -ne 0 ]]; then
  echo "One or more FASE 74.3 budgets were exceeded (or could not be verified)."
  exit 1
fi
echo "All FASE 74.3 budgets satisfied."
