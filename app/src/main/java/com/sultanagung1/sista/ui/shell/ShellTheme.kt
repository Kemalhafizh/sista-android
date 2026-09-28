package com.sultanagung1.sista.ui.shell

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import com.sultanagung1.sista.core.ui.theme.SistaTheme

/**
 * Design system v2 inside the current app theme: follows the light/dark
 * choice the user made in Settings (ThemeManager), not only the system's.
 * Used by every rebuilt surface until the whole app runs on SistaTheme.
 */
@Composable
fun ShellTheme(content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    SistaTheme(darkTheme = dark, content = content)
}
