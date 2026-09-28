package com.sultanagung1.sista.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.sultanagung1.sista.core.designsystem.R
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.core.ui.component.ButtonSize
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone

/**
 * One sign-in for everyone: students, teachers, parents and staff use the
 * same form. There is no role picker — the server knows the account's role
 * and the app builds its menus from what the server says the account may
 * use (me/capabilities), so choosing a "role" here would decide nothing.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val haptics = rememberHapticFeedbackHelper()

    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.rememberedIdentifier) {
        val remembered = uiState.rememberedIdentifier
        if (!remembered.isNullOrBlank() && identifier.isBlank()) identifier = remembered
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            haptics.success()
            onLoginSuccess()
        }
    }

    val submit = {
        focusManager.clearFocus()
        haptics.tapHeavy()
        viewModel.login(identifier.trim(), password)
    }

    // Offered only once this device has a key registered for a remembered
    // account (see LoginViewModel.loginWithBiometric); the prompt unlocks a
    // Keystore signing key, never a stored password.
    val activity = context as? FragmentActivity
    val onBiometric: (() -> Unit)? =
        if (activity != null && uiState.isBiometricEnabled && !uiState.rememberedUserId.isNullOrBlank()) {
            {
                focusManager.clearFocus()
                BiometricVault.authenticate(
                    activity = activity,
                    title = "Masuk dengan sidik jari",
                    subtitle = "Sentuh sensor sidik jari untuk masuk",
                    negativeButtonText = "Pakai kata sandi",
                    onSuccess = {
                        haptics.success()
                        viewModel.loginWithBiometric()
                    },
                    onError = { haptics.errorWarning() },
                )
            }
        } else {
            null
        }

    LoginContent(
        identifier = identifier,
        onIdentifierChange = { identifier = it },
        password = password,
        onPasswordChange = { password = it },
        loading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onDismissError = viewModel::clearError,
        onSubmit = submit,
        onBiometric = onBiometric,
    )
}

/** The sign-in form without any state of its own, so it can be previewed and screenshot-tested. */
@Composable
fun LoginContent(
    identifier: String,
    onIdentifierChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    loading: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onSubmit: () -> Unit,
    onBiometric: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ShellTheme {
        Surface(modifier = modifier.fillMaxSize(), color = SistaTheme.colors.background) {
            Box(contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .systemBarsPadding()
                        .imePadding()
                        .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
                ) {
                    SchoolHeader()
                    Spacer(Modifier.height(Spacing.xxxl))

                    Text(
                        "Masuk ke akun Anda",
                        style = SistaTheme.typography.headlineSmall,
                        color = SistaTheme.colors.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Satu akun untuk siswa, guru, orang tua, dan staf sekolah. " +
                            "Menu akan menyesuaikan peran akun Anda.",
                        style = SistaTheme.typography.bodyMedium,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.xl))

                    if (errorMessage != null) {
                        InlineBanner(
                            message = errorMessage,
                            tone = StatusTone.Danger,
                            title = "Belum bisa masuk",
                            onDismiss = onDismissError,
                            modifier = Modifier.testTag("login_error"),
                        )
                        Spacer(Modifier.height(Spacing.lg))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        SistaTextField(
                            value = identifier,
                            onValueChange = onIdentifierChange,
                            label = "NISN, NIP, atau email",
                            leadingIcon = Icons.Outlined.Person,
                            enabled = !loading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier.testTag("login_identifier_input"),
                        )
                        SistaTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            label = "Kata sandi",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            enabled = !loading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                            modifier = Modifier.testTag("login_password_input"),
                        )
                    }
                    Spacer(Modifier.height(Spacing.xl))

                    SistaButton(
                        text = "Masuk",
                        onClick = onSubmit,
                        size = ButtonSize.Large,
                        fullWidth = true,
                        loading = loading,
                        enabled = identifier.isNotBlank() && password.isNotEmpty(),
                        modifier = Modifier.testTag("login_submit_button"),
                    )

                    if (onBiometric != null) {
                        OrDivider()
                        SistaButton(
                            text = "Masuk dengan sidik jari",
                            onClick = onBiometric,
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Large,
                            leadingIcon = Icons.Outlined.Fingerprint,
                            fullWidth = true,
                            enabled = !loading,
                            modifier = Modifier.testTag("login_biometric_button"),
                        )
                    }

                    Spacer(Modifier.height(Spacing.xxl))
                    Text(
                        "Lupa kata sandi? Hubungi tata usaha atau admin sekolah untuk mengatur ulang.",
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SchoolHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(SistaTheme.shapes.large)
                .background(SistaTheme.colors.primaryContainer)
                .padding(Spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_kotak),
                contentDescription = "Logo SMA Islam Sultan Agung 1",
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.size(Spacing.md))
        Column {
            Text("Sulaone", style = SistaTheme.typography.titleLarge, color = SistaTheme.colors.onSurface)
            Text(
                "SMA Islam Sultan Agung 1 Semarang",
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.padding(vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(Modifier.weight(1f), color = SistaTheme.colors.outlineVariant)
        Text(
            "atau",
            style = SistaTheme.typography.labelMedium,
            color = SistaTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        HorizontalDivider(Modifier.weight(1f), color = SistaTheme.colors.outlineVariant)
    }
}
