package com.sultanagung1.sista.e2e

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import org.junit.After
import org.junit.Before
import java.net.URL
import java.time.Duration

/**
 * FASE 74.1: shared Appium/ADB plumbing for the two E2E scenarios in the
 * roadmap (GPS attendance flow, CBT offline resilience). Requires, on the
 * machine actually running this suite (not this sandbox — see e2e/README.md):
 *  - An Appium server reachable at APPIUM_URL (default http://127.0.0.1:4723)
 *  - A running emulator/device with the debug APK already installed
 *  - `adb` on PATH
 *
 * None of the three (Appium server, emulator, adb) exist in the sandbox this
 * suite was authored in, so it has been reviewed carefully for correctness
 * against the real Appium Java client 9.x / UiAutomator2Options API, but has
 * not been executed. Treat a first real run as a shakedown run.
 */
abstract class AppiumTestBase {

    protected lateinit var driver: AndroidDriver

    companion object {
        const val APP_PACKAGE = "com.sultanagung1.sista"
        val APPIUM_URL: String = System.getenv("APPIUM_URL") ?: "http://127.0.0.1:4723"
        val DEVICE_UDID: String? = System.getenv("ANDROID_E2E_UDID") // null = Appium picks the sole attached device
    }

    @Before
    open fun setUpDriver() {
        val options = UiAutomator2Options()
            .setAppPackage(APP_PACKAGE)
            .setAppActivity("com.sultanagung1.sista.MainActivity")
            .setAutoGrantPermissions(true)
            // The app is already installed by the CI job (see
            // .github/workflows/e2e-pipeline.yml) — this suite drives an
            // existing install rather than re-installing an APK path, so
            // re-runs don't wipe app state (e.g. a logged-in session) that a
            // prior step in the same job may depend on.
            .setNoReset(true)
            .setNewCommandTimeout(Duration.ofSeconds(120))

        DEVICE_UDID?.let { options.setUdid(it) }

        driver = AndroidDriver(URL(APPIUM_URL), options)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
    }

    @After
    open fun tearDownDriver() {
        if (::driver.isInitialized) {
            driver.quit()
        }
    }

    /** Runs an adb shell command against the same device Appium is driving. */
    protected fun adbShell(vararg args: String): String {
        val udidArgs = DEVICE_UDID?.let { arrayOf("-s", it) } ?: emptyArray()
        val command = arrayOf("adb", *udidArgs, "shell", *args)
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output
    }

    /**
     * Injects a GPS fix via the emulator console's `geo fix` command — this
     * is the real, standard technique for feeding location to an emulator's
     * actual GPS/FusedLocationProvider (what GeofenceAttendanceScreen.kt
     * consumes via play-services-location) without the app needing any
     * test-only code of its own. Only works against an emulator (not a
     * physical device — see scripts/mock_location.sh for the physical-
     * device alternative via `adb shell appops set ... mock_location allow`
     * plus a mock-location provider app, which needs one extra manual step
     * the emulator path doesn't).
     *
     * NOTE: because GeofenceAttendanceScreen surfaces "Proteksi Anti-Fake
     * GPS & TEE" (isMockLocationDetected — see GeofenceAttendanceScreen.kt),
     * a real end-to-end run against the physical-device mock-location path
     * would legitimately trip that same anti-cheat flag the app is designed
     * to catch. The emulator `geo fix` path below is a genuine (non-mocked,
     * from the OS's point of view) GPS provider update instead, so it does
     * not trigger that flag — use it for this scenario's "happy path" run.
     */
    protected fun setGpsFix(latitude: Double, longitude: Double) {
        val udidArgs = DEVICE_UDID?.let { arrayOf("-s", it) } ?: emptyArray()
        val process = ProcessBuilder("adb", *udidArgs, "emu", "geo", "fix", longitude.toString(), latitude.toString())
            .redirectErrorStream(true)
            .start()
        process.waitFor()
    }

    protected fun setNetworkEnabled(enabled: Boolean) {
        val state = if (enabled) "enable" else "disable"
        adbShell("svc", "wifi", state)
        adbShell("svc", "data", state)
    }
}
