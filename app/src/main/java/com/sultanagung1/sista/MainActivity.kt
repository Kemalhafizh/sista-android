package com.sultanagung1.sista

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sultanagung1.sista.core.accessibility.FontScaleManager
import com.sultanagung1.sista.core.accessibility.LanguageManager
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.designsystem.SulaoneTheme
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    // The manifest's <uses-permission android:name="POST_NOTIFICATIONS" />
    // alone does nothing on Android 13+ — it's a runtime permission there,
    // so without this request, both FCM push and the local widgets/reminder
    // notifications this app dispatches would silently never show.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val sessionManager = SessionManager(applicationContext)
        val apiClient = ApiClient(applicationContext)
        val languageManager = LanguageManager(sessionManager, lifecycleScope)
        val fontScaleManager = FontScaleManager(sessionManager, lifecycleScope)
        val themeManager = ThemeManager(sessionManager, lifecycleScope)

        val initialDeepLinkRoute = com.sultanagung1.sista.core.deeplink.DeepLinkRouter.resolveRoute(intent?.data)

        // Apply Adaptive Display Refresh Rate (60Hz / 90Hz / 120Hz / LTPO)
        com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager.applyToActivity(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager
                    .getStoredModeFlow(sessionManager)
                    .collect { mode ->
                        com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager.applyToActivity(this@MainActivity, mode)
                    }
            }
        }

        // Telemetry: Synchronize User Context to Telemetry Hub
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                kotlinx.coroutines.flow.combine(
                    sessionManager.userRoleFlow,
                    sessionManager.userIdentifierFlow
                ) { role, identifier ->
                    Pair(role, identifier)
                }.collect { (role, identifier) ->
                    com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.setUserContext(
                        userId = identifier,
                        role = role
                    )
                }
            }
        }

        setContent {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                com.sultanagung1.sista.core.telemetry.AppStartupTracker.recordFirstDraw()
            }
            val themeMode by themeManager.themeMode.collectAsState()
            val isHighContrast by themeManager.isHighContrast.collectAsState()
            val currentLang by languageManager.currentLanguage.collectAsState()
            val fontScale by fontScaleManager.fontScale.collectAsState()
            val isDyslexicFriendly by fontScaleManager.isDyslexicFriendly.collectAsState()

            SulaoneTheme(
                themeMode = themeMode,
                isHighContrast = isHighContrast,
                fontScale = fontScale,
                isDyslexicFriendly = isDyslexicFriendly,
                language = currentLang
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides currentLang.layoutDirection
                ) {
                    // FASE 74.1: Compose's testTag() is invisible to an external
                    // UiAutomator2/Appium driver by default — it only lives in
                    // Compose's own semantics tree, not the native accessibility
                    // tree ADB/Appium walk. testTagsAsResourceId republishes every
                    // testTag as a real resource-id in that tree so E2E scripts can
                    // find elements (e.g. "geofence_checkin_button",
                    // "cbt_token_input") without brittle text/coordinate matching.
                    // Debug-only: it's a testing hook, not release-build behavior.
                    val rootModifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (BuildConfig.DEBUG) {
                                Modifier.semantics { testTagsAsResourceId = true }
                            } else {
                                Modifier
                            }
                        )
                    Box(modifier = rootModifier) {
                        AppNavigation(
                            apiClient = apiClient,
                            sessionManager = sessionManager,
                            languageManager = languageManager,
                            fontScaleManager = fontScaleManager,
                            themeManager = themeManager,
                            initialDeepLinkRoute = initialDeepLinkRoute
                        )
                    }
                }
            }
        }
    }
}
