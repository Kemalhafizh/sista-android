#!/usr/bin/env bash
# FASE 74.1 helper: feed a GPS fix to the device Appium is driving.
#
# On an EMULATOR, prefer `adb emu geo fix <lon> <lat>` directly (see
# AppiumTestBase.setGpsFix in the Kotlin suite) — it updates the emulator's
# real GPS provider, so GeofenceAttendanceScreen's own anti-mock-location
# flag (isMockLocationDetected) correctly stays false, exactly like a real
# phone moving to that location would.
#
# This script is for the PHYSICAL DEVICE path instead, which genuinely does
# go through Android's mock-location provider mechanism (there is no
# equivalent to `emu geo fix` on real hardware), and — unlike the emulator
# path — DOES set isMockLocationDetected=true when the app's own check
# reads it back. That is a real behavior difference, not a bug in this
# script: use the emulator path for "does the happy-path check-in work",
# and this path for "does the anti-fake-GPS banner actually trigger".
#
# Usage: mock_location.sh <package> <latitude> <longitude> [device_udid]
set -euo pipefail

PACKAGE="${1:?Usage: mock_location.sh <package> <latitude> <longitude> [device_udid]}"
LATITUDE="${2:?latitude required}"
LONGITUDE="${3:?longitude required}"
UDID="${4:-}"

ADB_ARGS=()
if [[ -n "$UDID" ]]; then
  ADB_ARGS+=(-s "$UDID")
fi

echo "Marking '$PACKAGE' as the mock-location app for API 23+..."
adb "${ADB_ARGS[@]}" shell appops set "$PACKAGE" android:mock_location allow

echo "NOTE: Android has no single adb-only command to then push a fake fix"
echo "through that provider without a small on-device helper (e.g. an"
echo "instrumentation using LocationManager.addTestProvider(), or a tool"
echo "like OpenGpx/Lockito). This script only grants the permission; wire"
echo "an actual fix-injection tool here for the physical-device path before"
echo "relying on it — do not assume this alone moves the GPS reading."
echo "For CI, use the emulator 'geo fix' path instead (no extra tooling)."
echo "  adb ${ADB_ARGS[*]} emu geo fix $LONGITUDE $LATITUDE"
