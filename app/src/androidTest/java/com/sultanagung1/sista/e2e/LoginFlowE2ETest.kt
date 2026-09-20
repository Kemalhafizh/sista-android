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
class LoginFlowE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_displaysCorrectly() {
        composeTestRule.onNode(hasText("NIS").or(hasText("Nomor Induk Siswa"))).assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Masuk").assertExists()
    }

    @Test
    fun loginScreen_emptyCredentials_showsError() {
        composeTestRule.onNodeWithText("Masuk").performClick()
        composeTestRule.onNodeWithText("NIS tidak boleh kosong").assertExists()
    }

    @Test
    fun loginScreen_validCredentials_navigatesToHome() {
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
    }
}
