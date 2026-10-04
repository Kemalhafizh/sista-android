package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.feature.profile.R
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsFormatTest {

    @Test
    fun everySliderStopHasALabel() {
        // The old slider had six stops and five labels (175% was missing).
        assertEquals(listOf("75%", "100%", "125%", "150%", "175%", "200%"), TEXT_SCALES.map(::scaleLabel))
    }

    @Test
    fun savedValuesLandOnAStop() {
        assertEquals(1.25f, nearestScale(1.3f))
        assertEquals(0.75f, nearestScale(0.1f))
        assertEquals(2.0f, nearestScale(3f))
    }

    @Test
    fun everyThemeIsOffered() {
        assertEquals(AppThemeMode.entries.toSet(), SETTINGS_THEMES.toSet())
        // AMOLED is honest about LCD screens.
        assertEquals(R.string.theme_hint_amoled, themeHint(AppThemeMode.AMOLED_BLACK))
    }
}
