package com.sultanagung1.sista.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

internal val SistaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** 4-point spacing scale. Layouts use these, never ad-hoc dp values. */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp

    /** Horizontal padding of screen content. */
    val screen = 16.dp
}

/**
 * Root theme of the rebuilt app. The brand palette is fixed (no dynamic
 * colour): the school's emerald identity should look the same on every phone.
 */
@Composable
fun SistaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = SistaTypography,
            shapes = SistaShapes,
            content = content,
        )
    }
}

/** Shorthand accessors so screens read `SistaTheme.colors.primary`, `SistaTheme.extendedColors.success`. */
object SistaTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme
    val extendedColors: ExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography
    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes
}
