# 🚀 SISTA Android Release Checklist

## Pre-Release Verification

### Code Quality
- [ ] `./gradlew lint` — zero critical issues
- [ ] `./gradlew testDebugUnitTest` — all unit tests passing
- [ ] `./gradlew connectedDebugAndroidTest` — all E2E tests passing (requires device/emulator)
- [ ] No compiler errors (`./gradlew assembleDebug` builds successfully)
- [ ] No hardcoded `Color(0x...)` in `ui/` directory (use design system tokens)
- [ ] All TODO/FIXME comments reviewed and addressed

### Network & Sync Integrity
- [ ] Network Interceptor verified all API endpoints responding with 2xx
- [ ] Offline queue processes all pending actions on reconnect
- [ ] Idempotency keys prevent duplicate submissions
- [ ] WebSocket reconnection works after airplane mode toggle

### Database Validation (Run via PostgreSQL MCP or psql)
- [ ] `validate_sync_integrity.sql` — all queries return 0 rows
- [ ] `validate_billing_mutations.sql` — all queries return 0 rows
- [ ] `validate_academic_data.sql` — all queries return 0 rows

### Security
- [ ] Biometric authentication works (fingerprint/face)
- [ ] CBT anti-cheat lockdown blocks screenshots and back navigation
- [ ] Auth token refresh works after expiry
- [ ] No sensitive data logged in production builds

### Performance
- [ ] APK size delta < 5% from previous release
- [ ] Cold start time < 2 seconds on mid-range device
- [ ] Smooth 60fps scrolling on all list screens
- [ ] Memory usage stable (no leaks after 10-minute session)

## Build & Signing
- [ ] Update `versionCode` and `versionName` in `app/build.gradle`
- [ ] Generate signed APK/AAB with release keystore
- [ ] ProGuard/R8 mapping file archived for crash symbolication
- [ ] Firebase Crashlytics mapping file uploaded

## Post-Release
- [ ] Git tag created: `v{versionName}-{versionCode}`
- [ ] Release notes published
- [ ] Play Store listing updated (if applicable)
- [ ] Monitor Firebase Crashlytics for new crash clusters (24h window)
