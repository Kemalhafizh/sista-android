package com.sultanagung1.sista.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import kotlin.math.roundToInt

/** The text sizes offered: exactly the slider's stops. */
internal val TEXT_SCALES = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

/** "125%". The slider used to label five stops while offering six. */
internal fun scaleLabel(scale: Float): String = "${(scale * 100).roundToInt()}%"

/** The nearest offered size, so a saved odd value still lands on a stop. */
internal fun nearestScale(scale: Float): Float = TEXT_SCALES.minBy { kotlin.math.abs(it - scale) }

@Composable
fun AccessibilitySettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val fontScale by viewModel.fontScale.collectAsState()
    val dyslexic by viewModel.isDyslexicFriendly.collectAsState()
    val highContrast by viewModel.isHighContrast.collectAsState()
    AccessibilitySettingsContent(
        fontScale = fontScale,
        dyslexicFriendly = dyslexic,
        highContrast = highContrast,
        onFontScale = viewModel::setFontScale,
        onDyslexicFriendly = viewModel::setDyslexicFriendly,
        onHighContrast = viewModel::setHighContrast,
        onNavigateBack = onNavigateBack,
    )
}

/** The accessibility page without a ViewModel, for previews and screenshots. */
@Composable
fun AccessibilitySettingsContent(
    fontScale: Float,
    dyslexicFriendly: Boolean,
    highContrast: Boolean,
    onFontScale: (Float) -> Unit,
    onDyslexicFriendly: (Boolean) -> Unit,
    onHighContrast: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = "Aksesibilitas", onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("accessibility_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                item(key = "size") {
                    val current = nearestScale(fontScale)
                    SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ukuran teks", style = SistaTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(scaleLabel(current), style = SistaTheme.typography.titleMedium, color = SistaTheme.colors.primary)
                        }
                        Slider(
                            value = current,
                            onValueChange = { onFontScale(nearestScale(it)) },
                            valueRange = TEXT_SCALES.first()..TEXT_SCALES.last(),
                            steps = TEXT_SCALES.size - 2,
                            modifier = Modifier.semantics {
                                contentDescription = "Ukuran teks"
                                stateDescription = scaleLabel(current)
                            },
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TEXT_SCALES.forEach {
                                Text(scaleLabel(it), style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant)
                            }
                        }
                        Text(
                            "Ditambah ke ukuran huruf yang diatur di HP.",
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                }

                item(key = "reading") {
                    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                        ToggleRow(
                            title = "Ramah disleksia",
                            body = "Jarak antarhuruf dan antarbaris lebih lebar.",
                            checked = dyslexicFriendly,
                            onChange = onDyslexicFriendly,
                        )
                        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                        ToggleRow(
                            title = "Kontras tinggi",
                            body = "Teks tanpa warna abu-abu dan garis lebih tegas.",
                            checked = highContrast,
                            onChange = onHighContrast,
                        )
                    }
                }

                item(key = "preview_header") { SectionHeader("Pratinjau", Modifier.padding(top = Spacing.md)) }
                item(key = "preview") {
                    SistaCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Contoh judul", style = SistaTheme.typography.titleMedium)
                        Text(
                            "Seperti inilah teks di aplikasi dengan pengaturan di atas. Ubah ukuran atau mode baca lalu lihat bedanya di sini.",
                            style = SistaTheme.typography.bodyMedium,
                            color = SistaTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = Spacing.md)) {
            Text(title, style = SistaTheme.typography.bodyLarge)
            Text(body, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
