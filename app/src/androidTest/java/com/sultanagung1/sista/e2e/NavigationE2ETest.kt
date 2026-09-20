package com.sultanagung1.sista.e2e

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.sultanagung1.sista.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        composeTestRule.onNode(hasText("NIS").or(hasText("Nomor Induk Siswa")))
            .performTextInput("12345")
        composeTestRule.onNodeWithText("Password")
            .performTextInput("password123")
        composeTestRule.onNodeWithText("Masuk").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun bottomNavigation_allTabs_navigate() {
        composeTestRule.onNodeWithText("Beranda").performClick()
        composeTestRule.onNodeWithText("Assalamu'alaikum", substring = true).assertExists()
        
        composeTestRule.onNodeWithText("Jadwal").performClick()
        composeTestRule.onNodeWithText("Jadwal Pelajaran").assertExists()
        
        composeTestRule.onNodeWithText("Notifikasi").performClick()
        composeTestRule.onNodeWithText("Pusat Notifikasi").assertExists()
        
        composeTestRule.onNodeWithText("Profil").performClick()
        composeTestRule.onNodeWithText("Profil Siswa").assertExists()
    }

    @Test
    fun deepLink_billing_navigatesCorrectly() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sulaone://billing"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ApplicationProvider.getApplicationContext<android.content.Context>().startActivity(intent)
        
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Keuangan", substring = true).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun backNavigation_returnsToParent() {
        composeTestRule.onNodeWithText("Keuangan").performClick()
        composeTestRule.waitForIdle()
        
        androidx.test.espresso.Espresso.pressBack()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Assalamu'alaikum", substring = true).assertExists()
    }
}
