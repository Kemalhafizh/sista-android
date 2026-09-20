package com.sultanagung1.sista

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.sultanagung1.sista.core.accessibility.FontScaleManager
import com.sultanagung1.sista.core.accessibility.LanguageManager
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.designsystem.SulaoneTheme
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
