package com.sultanagung1.sista.e2e

import io.appium.java_client.AppiumBy
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

/**
 * FASE 74.1, Skenario 2 — Alur Ujian CBT & Ketahanan Offline.
 *
 * 1. Login siswa -> Home -> "Ujian CBT" -> pilih ujian pertama pada daftar
 *    -> masukkan token 6 digit -> mulai ujian.
 * 2. Jawab beberapa soal pertama.
 * 3. Matikan koneksi (`svc wifi disable && svc data disable` via adb) ->
 *    lanjutkan menjawab -> verifikasi TIDAK ADA dialog error fatal yang
 *    menutup aplikasi saat "Kumpulkan" ditekan; karena CbtViewModel sudah
 *    punya jalur queueCbtSubmit() untuk kondisi offline (lihat
 *    CbtViewModel.kt), submit offline harus mendarat di dialog
 *    "cbt_submitted_offline_dialog" (Disimpan Offline), bukan crash.
 * 4. Nyalakan kembali koneksi -> jalankan sync manual -> jawaban yang
 *    tersimpan offline harus tersinkron (diverifikasi lewat SyncManager di
 *    NetworkContractValidationTest/PHPUnit, bukan diklaim ulang di sini
 *    tanpa bukti — lihat README untuk pembagian tanggung jawab).
 *
 * Requires a real exam token from a seeded active tryout/exam + student
 * account; see e2e/README.md.
 */
class CbtOfflineResilienceE2ETest : AppiumTestBase() {

    companion object {
        val STUDENT_ID: String = System.getenv("E2E_STUDENT_ID") ?: error(
            "Set E2E_STUDENT_ID to a real seeded student login before running this suite."
        )
        val STUDENT_PASSWORD: String = System.getenv("E2E_STUDENT_PASSWORD") ?: error(
            "Set E2E_STUDENT_PASSWORD for the E2E_STUDENT_ID account before running this suite."
        )
        val EXAM_TOKEN: String = System.getenv("E2E_CBT_EXAM_TOKEN") ?: error(
            "Set E2E_CBT_EXAM_TOKEN to a real 6-digit token from a currently-active exam window."
        )
    }

    @Test
    fun answersSurviveGoingOfflineMidExamAndDoNotCrashOnSubmit() {
        loginAsStudent()

        driver.findElement(
            AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))" +
                    ".scrollIntoView(new UiSelector().textContains(\"Ujian CBT\"))"
            )
        ).click()

        // CbtExamListScreen: pick the first listed exam — this suite verifies
        // flow resilience, not exam selection logic, so any exam whose token
        // matches EXAM_TOKEN works.
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().clickable(true).instance(0)")).click()

        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_token_input\")"))
            .sendKeys(EXAM_TOKEN)
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_start_exam_button\")")).click()

        // Answer questions 1-5 while online.
        repeat(5) {
            answerCurrentQuestionAndAdvance()
        }

        // Go offline exactly like the roadmap prescribes.
        setNetworkEnabled(enabled = false)

        try {
            // Continue answering 6-10 while offline — the app must not
            // throw up a fatal error dialog that closes the app.
            repeat(5) {
                answerCurrentQuestionAndAdvance(allowMissingNext = true)
            }

            // Submit while still offline.
            val collectButton = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_collect_button\")")
            )
            if (collectButton.isNotEmpty()) {
                collectButton.first().click()
                driver.findElement(
                    AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_confirm_submit_button\")")
                ).click()
            }

            // The real assertion for this scenario: the app is still
            // responsive (no crash / fatal dialog), and if the submit flow
            // ran, it honestly reported an offline queue rather than
            // pretending a normal online success happened.
            val appStillResponsive = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().packageName(\"$APP_PACKAGE\")")
            ).isNotEmpty()
            assertTrue("App must remain responsive after answering offline and submitting", appStillResponsive)

            val offlineDialogShown = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_submitted_offline_dialog\")")
            ).isNotEmpty()
            if (collectButton.isNotEmpty()) {
                assertTrue(
                    "Submitting while offline must show the honest 'Disimpan Offline' dialog, not a fatal error",
                    offlineDialogShown
                )
            }
        } finally {
            // Always restore connectivity, even if an assertion above fails,
            // so this suite doesn't leave the device/emulator offline for
            // whatever test runs next.
            setNetworkEnabled(enabled = true)
        }
    }

    private fun loginAsStudent() {
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"login_identifier_input\")"))
            .sendKeys(STUDENT_ID)
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"login_password_input\")"))
            .sendKeys(STUDENT_PASSWORD)
        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"login_submit_button\")")).click()
        driver.findElement(
            AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"home_screen_root\")")
        )
    }

    /**
     * Selects option A (any valid answer works for this suite's purpose —
     * it tests flow continuity, not scoring) and taps whichever of
     * "Selanjutnya" / "Kumpulkan" is showing. When [allowMissingNext] is
     * true and neither button appears (e.g. offline and the exam has fewer
     * than 10 questions), this is a no-op rather than a hard failure.
     */
    private fun answerCurrentQuestionAndAdvance(allowMissingNext: Boolean = false) {
        val options = driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_option_A\")"))
        if (options.isNotEmpty()) {
            options.first().click()
        }

        val next = driver.findElements(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"cbt_next_button\")"))
        if (next.isNotEmpty()) {
            next.first().click()
            return
        }
        if (!allowMissingNext) {
            error("Expected cbt_next_button to be present while answering questions 1-5")
        }
    }
}
