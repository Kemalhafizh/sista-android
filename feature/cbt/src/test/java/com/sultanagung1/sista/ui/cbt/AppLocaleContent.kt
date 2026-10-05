package com.sultanagung1.sista.ui.cbt

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.accessibility.AppLocale

/**
 * Screenshot content as the app shows it. The app runs Arabic as "ar-u-nu-latn"
 * (Latin digits, AppLocale.localeOf) with supportsRtl; a qualifier can carry
 * neither, so both are applied here when the qualifier's language is Arabic.
 */
@Composable
internal fun AppLocaleContent(dark: Boolean = false, content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val rtl = config.locales[0].language == "ar"
    val context = LocalContext.current
    val appContext = remember(rtl) {
        if (!rtl) context else context.createConfigurationContext(
            Configuration(config).apply { setLocale(AppLocale.localeOf(AppLanguage.ARABIC)) }
        )
    }
    CompositionLocalProvider(
        LocalContext provides appContext,
        LocalConfiguration provides appContext.resources.configuration,
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() }
    }
}
