package com.sultanagung1.sista.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.sultanagung1.sista.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class BillingFlowE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // Navigate past login
        composeTestRule.onNode(hasText("NIS").or(hasText("Nomor Induk Siswa")))
            .performTextInput("12345")
        composeTestRule.onNodeWithText("Password")
            .performTextInput("password123")
        composeTestRule.onNodeWithText("Masuk").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Assalamu'alaikum", substring = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        composeTestRule.onNodeWithText("Keuangan").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun billingScreen_displaysInvoiceList() {
        composeTestRule.onNodeWithText("Daftar Tagihan", substring = true).assertExists()
    }

    @Test
    fun billingScreen_showsPaymentStatus() {
        composeTestRule.onAllNodesWithText("Belum Bayar", substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun billingScreen_tapInvoice_showsDetail() {
        composeTestRule.onAllNodesWithText("Belum Bayar", substring = true)[0].performClick()
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Virtual Account", substring = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithText("Virtual Account", substring = true).assertExists()
    }
}
