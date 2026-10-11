package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.feature.profile.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import kotlin.math.roundToInt

/** The text sizes offered: exactly the slider's stops. */
internal val TEXT_SCALES = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

/** "125%". The slider used to label five stops while offering six. */
internal fun scaleLabel(scale: Float): String = "${(scale * 100).roundToInt()}%"

/** The nearest offered size, so a saved odd value still lands on a stop. */
internal fun nearestScale(scale: Float): Float = TEXT_SCALES.minBy { kotlin.math.abs(it - scale) }

/** Text size: one stop per offered size, with the current size beside the title. */
@Composable
internal fun TextSizeCard(fontScale: Float, onFontScale: (Float) -> Unit, modifier: Modifier = Modifier) {
    val current = nearestScale(fontScale)
    val textSizeLabel = stringResource(R.string.a11y_text_size)
    SistaCard(modifier = modifier.fillMaxWidth().testTag("settings_text_size")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(textSizeLabel, style = SistaTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(scaleLabel(current), style = SistaTheme.typography.titleMedium, color = SistaTheme.colors.primary)
        }
        Slider(
            value = current,
            onValueChange = { onFontScale(nearestScale(it)) },
            valueRange = TEXT_SCALES.first()..TEXT_SCALES.last(),
            steps = TEXT_SCALES.size - 2,
            modifier = Modifier.semantics {
                contentDescription = textSizeLabel
                stateDescription = scaleLabel(current)
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TEXT_SCALES.forEach {
                Text(scaleLabel(it), style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant)
            }
        }
        Text(
            stringResource(R.string.a11y_text_size_hint),
            style = SistaTheme.typography.bodySmall,
            color = SistaTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

/** Dyslexia-friendly text and high contrast, applied to the whole app at once. */
@Composable
internal fun ReadingCard(
    dyslexicFriendly: Boolean,
    highContrast: Boolean,
    onDyslexicFriendly: (Boolean) -> Unit,
    onHighContrast: (Boolean) -> Unit,
) {
    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
        ToggleRow(
            title = stringResource(R.string.a11y_dyslexia),
            body = stringResource(R.string.a11y_dyslexia_hint),
            checked = dyslexicFriendly,
            onChange = onDyslexicFriendly,
        )
        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
        ToggleRow(
            title = stringResource(R.string.a11y_high_contrast),
            body = stringResource(R.string.a11y_high_contrast_hint),
            checked = highContrast,
            onChange = onHighContrast,
        )
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
