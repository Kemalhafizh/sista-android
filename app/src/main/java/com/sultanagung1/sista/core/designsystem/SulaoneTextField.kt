package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * FASE 70.2 — Sulaone's atomic text field: floating label (native M3
 * OutlinedTextField behavior), animated inline validation error, and
 * one-line trailing-icon shortcuts (password visibility, clear text,
 * scanner) instead of hand-building a `trailingIcon` composable at every
 * call site. Passing [trailingIcon] explicitly still overrides all three
 * shortcuts, so both existing call sites keep working unchanged.
 *
 * Note: the border stays at Material 3's default thickness (1dp unfocused /
 * 2dp focused) — thinning it to 0.5dp requires M3's `container` decoration
 * slot, which this codebase's pinned Compose BOM support is unverified without
 * a working build in this environment, so it was left alone rather than risk
 * shipping code that cannot be verified to compile.
 */
@Composable
fun SulaoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    maxCharacters: Int? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    onClear: (() -> Unit)? = null,
    onScannerClick: (() -> Unit)? = null
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val resolvedTrailingIcon: @Composable (() -> Unit)? = trailingIcon ?: when {
        isPassword -> {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan"
                    )
                }
            }
        }
        onScannerClick != null -> {
            {
                IconButton(onClick = onScannerClick) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Pindai")
                }
            }
        }
        onClear != null && value.isNotEmpty() -> {
            {
                IconButton(onClick = onClear) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus teks")
                }
            }
        }
        else -> null
    }

    val visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (maxCharacters == null || it.length <= maxCharacters) {
                    onValueChange(it)
                }
            },
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = resolvedTrailingIcon,
            visualTransformation = visualTransformation,
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald700,
                focusedLabelColor = Emerald700,
                errorBorderColor = AccentRose,
                errorLabelColor = AccentRose,
                errorLeadingIconColor = AccentRose,
                errorTrailingIconColor = AccentRose
            ),
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = (isError && errorMessage != null) || helperText != null || maxCharacters != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (isError && errorMessage != null) {
                            Text(
                                text = errorMessage,
                                color = AccentRose,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (helperText != null) {
                            Text(
                                text = helperText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (maxCharacters != null) {
                        Text(
                            text = "${value.length} / $maxCharacters",
                            color = if (value.length >= maxCharacters) AccentRose else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
