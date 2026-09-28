package com.sultanagung1.sista.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sultanagung1.sista.data.model.GateDecision
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

/** What a gate screen can do: provided once by the app shell. */
@Immutable
data class GateActions(
    val onBack: () -> Unit = {},
    val onHome: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

val LocalGateActions = staticCompositionLocalOf { GateActions() }

/**
 * The only way to declare a destination in this app. The screen is shown
 * only when the account's server-given features open [route]
 * (FeatureCatalog), whichever way it was reached: menu, deep link,
 * notification or a typed route. Otherwise a clear "not available" page is
 * shown and the screen (and its API calls) never runs.
 */
fun NavGraphBuilder.guardedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route = route, arguments = arguments) { entry ->
        FeatureGate(route) { content(entry) }
    }
}

@Composable
fun FeatureGate(route: String, content: @Composable () -> Unit) {
    when (FeatureCatalog.decide(LocalCapabilityState.current, route)) {
        GateDecision.OPEN -> content()
        GateDecision.WAITING -> GateFrame { SkeletonList(Modifier.padding(Spacing.screen)) }
        GateDecision.UNKNOWN -> GateFrame {
            ErrorState(
                title = "Belum bisa memeriksa akses",
                body = "Daftar fitur akun Anda belum termuat. Periksa koneksi internet, lalu coba lagi.",
                icon = Icons.Outlined.CloudOff,
                onRetry = LocalGateActions.current.onRetry,
            )
        }
        GateDecision.LOCKED -> GateFrame {
            EmptyState(
                title = if (FeatureCatalog.requiredFor(route) == setOf(FeatureCatalog.NOT_READY)) {
                    "Fitur ini belum tersedia"
                } else {
                    "Tidak tersedia untuk akun Anda"
                },
                body = "Fitur yang tampil menyesuaikan peran akun Anda di sekolah. " +
                    "Hubungi admin sekolah bila menurut Anda ini keliru.",
                icon = Icons.Outlined.Lock,
                actionLabel = "Ke Beranda",
                onAction = LocalGateActions.current.onHome,
                modifier = Modifier.testTag("feature_locked"),
            )
        }
    }
}

@Composable
private fun GateFrame(content: @Composable () -> Unit) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = "", onBack = LocalGateActions.current.onBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(SistaTheme.colors.background)
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { content() }
        }
    }
}
