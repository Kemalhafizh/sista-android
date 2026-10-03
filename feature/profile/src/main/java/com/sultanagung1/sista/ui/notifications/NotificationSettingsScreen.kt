package com.sultanagung1.sista.ui.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.NotificationChannelType

/** One push category as Android reports it. */
data class ChannelStatus(val type: NotificationChannelType, val enabled: Boolean)

/** What this phone currently lets SISTA show. */
data class NotificationSystemState(
    val appEnabled: Boolean,
    /** Android 13+ asks for permission; older versions only have the settings page. */
    val canAskPermission: Boolean,
    val channels: List<ChannelStatus>,
)

/**
 * Notification settings are Android's own: whether SISTA may notify at all
 * and, per category, whether that channel is on. These are what actually
 * decide if a push appears, so the screen reads them and opens the system
 * page to change them. In-app notifications are kept either way.
 */
@Composable
fun NotificationSettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(readSystemState(context)) }

    // Read again when the user comes back from Android's settings.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) state = readSystemState(context) }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Refused for good: Android no longer shows the dialog, so open the page instead.
        if (!granted) context.startActivity(appSettingsIntent(context))
        state = readSystemState(context)
    }

    NotificationSettingsContent(
        state = state,
        onAllow = {
            if (state.canAskPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                context.startActivity(appSettingsIntent(context))
            }
        },
        onOpenAppSettings = { context.startActivity(appSettingsIntent(context)) },
        onOpenChannel = { context.startActivity(channelSettingsIntent(context, it.channelId)) },
        onNavigateBack = onNavigateBack,
    )
}

/** The settings screen without Android, for previews and screenshots. */
@Composable
fun NotificationSettingsContent(
    state: NotificationSystemState,
    onAllow: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenChannel: (NotificationChannelType) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = "Pengaturan notifikasi", onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("notification_settings_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "status") { AppStatus(state, onAllow, onOpenAppSettings) }
                item(key = "channels_header") { SectionHeader("Kategori") }
                items(state.channels, key = { it.type.channelId }) { channel ->
                    ChannelRow(channel, appEnabled = state.appEnabled, onClick = { onOpenChannel(channel.type) })
                }
                item(key = "note") {
                    Text(
                        "Mematikan kategori hanya menyembunyikan pemberitahuan di layar ponsel. Semua notifikasi tetap tersimpan di halaman Notifikasi.",
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppStatus(state: NotificationSystemState, onAllow: () -> Unit, onOpenAppSettings: () -> Unit) {
    if (state.appEnabled) {
        SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenAppSettings) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                IconBadge(icon = Icons.Outlined.NotificationsActive, tone = StatusTone.Success)
                Column(Modifier.weight(1f)) {
                    Text("Notifikasi aktif", style = SistaTheme.typography.titleSmall)
                    Text(
                        "${state.channels.count { it.enabled }} dari ${state.channels.size} kategori menyala",
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = SistaTheme.colors.onSurfaceVariant)
            }
        }
    } else {
        InlineBanner(
            title = "Notifikasi SISTA dimatikan",
            message = "Kabar presensi, tagihan, dan siaran darurat tidak akan muncul di layar ponsel.",
            tone = StatusTone.Danger,
        )
        SistaButton(
            text = if (state.canAskPermission) "Izinkan notifikasi" else "Buka pengaturan Android",
            onClick = onAllow,
            leadingIcon = Icons.Outlined.NotificationsActive,
            fullWidth = true,
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

@Composable
private fun ChannelRow(channel: ChannelStatus, appEnabled: Boolean, onClick: () -> Unit) {
    val on = appEnabled && channel.enabled
    SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconBadge(icon = channel.type.icon(), tone = if (on) channel.type.tone() else StatusTone.Neutral)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(channel.type.label, style = SistaTheme.typography.titleSmall)
                Text(channel.type.channelDesc, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
            }
            StatusPill(text = if (on) "Aktif" else "Mati", tone = if (on) StatusTone.Success else StatusTone.Neutral)
        }
    }
}

internal fun readSystemState(context: Context): NotificationSystemState {
    val manager = context.getSystemService(NotificationManager::class.java)
    val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    return NotificationSystemState(
        appEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
        canAskPermission = !granted,
        // A channel not created yet has its default importance, which is on.
        channels = NotificationChannelType.values().map { type ->
            ChannelStatus(type, manager?.getNotificationChannel(type.channelId)?.importance != NotificationManager.IMPORTANCE_NONE)
        },
    )
}

private fun appSettingsIntent(context: Context) =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

private fun channelSettingsIntent(context: Context, channelId: String) =
    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
