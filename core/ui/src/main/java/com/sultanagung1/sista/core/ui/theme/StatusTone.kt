package com.sultanagung1.sista.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** Meaning of a status, independent of where it is shown. */
enum class StatusTone { Neutral, Brand, Success, Warning, Danger, Info }

@Immutable
data class ToneColors(val content: Color, val container: Color, val onContainer: Color)

/** The colours for a [StatusTone] in the current theme. */
@Composable
@ReadOnlyComposable
fun StatusTone.colors(): ToneColors {
    val c = SistaTheme.colors
    val x = SistaTheme.extendedColors
    return when (this) {
        StatusTone.Neutral -> ToneColors(c.onSurfaceVariant, c.surfaceContainerHighest, c.onSurface)
        StatusTone.Brand -> ToneColors(c.primary, c.primaryContainer, c.onPrimaryContainer)
        StatusTone.Success -> ToneColors(x.success, x.successContainer, x.onSuccessContainer)
        StatusTone.Warning -> ToneColors(x.warning, x.warningContainer, x.onWarningContainer)
        StatusTone.Danger -> ToneColors(c.error, c.errorContainer, c.onErrorContainer)
        StatusTone.Info -> ToneColors(x.info, x.infoContainer, x.onInfoContainer)
    }
}
