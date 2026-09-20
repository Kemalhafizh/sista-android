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
class HomeScreenE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // Navigate past login for test setup
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

    @Test
    fun homeScreen_heroSection_isDisplayed() {
        composeTestRule.onNodeWithText("Assalamu'alaikum", substring = true).assertExists()
    }

    @Test
    fun homeScreen_quickActions_areDisplayed() {
        composeTestRule.onNodeWithText("Presensi").assertExists()
        composeTestRule.onNodeWithText("CBT").assertExists()
    }

    @Test
    fun homeScreen_bottomNavigation_works() {
        composeTestRule.onNodeWithText("Jadwal").performClick()
        composeTestRule.onNodeWithText("Jadwal Pelajaran").assertExists()

        composeTestRule.onNodeWithText("Profil").performClick()
        composeTestRule.onNodeWithText("Profil Siswa").assertExists()
    }
}
