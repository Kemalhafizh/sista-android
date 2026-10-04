package com.sultanagung1.sista.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

enum class ButtonVariant { Primary, Secondary, Outlined, Text, Danger }

enum class ButtonSize(val minHeight: Int, val horizontalPadding: Int) {
    Medium(44, 20),
    Large(52, 24),
}

/**
 * The one button of the app. [loading] keeps the button's size, shows a
 * spinner in place of the icon and ignores clicks — no double submits.
 */
@Composable
fun SistaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
) {
    val processing = stringResource(R.string.core_processing)
    val sized = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .heightIn(min = size.minHeight.dp)
        .semantics { if (loading) stateDescription = processing }
    val padding = PaddingValues(horizontal = size.horizontalPadding.dp)
    val click = { if (!loading) onClick() }
    val label: @Composable () -> Unit = { ButtonLabel(text, leadingIcon, loading) }

    when (variant) {
        ButtonVariant.Primary -> Button(click, sized, enabled, contentPadding = padding) { label() }
        ButtonVariant.Secondary -> FilledTonalButton(click, sized, enabled, contentPadding = padding) { label() }
        ButtonVariant.Outlined -> OutlinedButton(click, sized, enabled, contentPadding = padding) { label() }
        ButtonVariant.Text -> TextButton(click, sized, enabled, contentPadding = padding) { label() }
        ButtonVariant.Danger -> Button(
            onClick = click,
            modifier = sized,
            enabled = enabled,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = SistaTheme.colors.error,
                contentColor = SistaTheme.colors.onError,
            ),
        ) { label() }
    }
}

@Composable
private fun ButtonLabel(text: String, icon: ImageVector?, loading: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
                Spacer(Modifier.width(Spacing.sm))
            }
            icon != null -> {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
        }
        Text(text, style = SistaTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
