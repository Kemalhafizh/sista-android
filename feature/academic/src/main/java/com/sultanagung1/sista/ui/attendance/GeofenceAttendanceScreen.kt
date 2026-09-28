package com.sultanagung1.sista.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.ui.component.ButtonSize
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.GeoUtils
import com.sultanagung1.sista.data.model.AttendanceCheckinResponse

/**
 * GPS check-in (`mobile/attendance/gps-checkin`). The position is a fresh
 * fix taken on this screen — never the phone's cached last location, which
 * can be hours old — and the check-in is only offered inside the school's
 * radius from the server (GET mobile/config), with a usable accuracy and no
 * mock-location app. The server checks all of it again.
 */
@SuppressLint("MissingPermission")
@Composable
fun GeofenceAttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = rememberHapticFeedbackHelper()
    val client = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

    var location by remember { mutableStateOf<LocationState>(if (hasPermission()) LocationState.Locating else LocationState.NeedsPermission) }
    var pending by remember { mutableStateOf<CancellationTokenSource?>(null) }

    fun requestFix() {
        pending?.cancel()
        val token = CancellationTokenSource().also { pending = it }
        location = LocationState.Locating
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0) // a position taken now, not one cached earlier
            .setDurationMillis(20_000)
            .build()
        client.getCurrentLocation(request, token.token)
            .addOnSuccessListener { fix ->
                location = if (fix == null) {
                    LocationState.Unavailable
                } else {
                    val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) fix.isMock
                    else @Suppress("DEPRECATION") fix.isFromMockProvider
                    viewModel.updateCoordinates(fix.latitude, fix.longitude, isMock)
                    LocationState.Fixed(fix.latitude, fix.longitude, fix.accuracy, isMock)
                }
            }
            .addOnFailureListener { location = LocationState.Unavailable }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) requestFix() else location = LocationState.PermissionDenied
    }

    LaunchedEffect(Unit) { if (hasPermission()) requestFix() }
    DisposableEffect(Unit) { onDispose { pending?.cancel() } }

    // Back from the system settings with the permission now granted: locate.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && location == LocationState.PermissionDenied && hasPermission()) requestFix()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val radius = GeoUtils.campus.radiusMeters
    val distance = (location as? LocationState.Fixed)?.let { uiState.distanceToCampusMeters }

    GeofenceAttendanceContent(
        location = location,
        status = GeofenceStatus.of(location, distance, radius),
        radiusMeters = radius,
        submitting = uiState.isLoading,
        result = uiState.checkinResult,
        queuedOffline = uiState.isQueuedOffline,
        errorMessage = uiState.errorMessage,
        onAllowLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        },
        onRefreshLocation = ::requestFix,
        onCheckIn = {
            val fix = location as? LocationState.Fixed ?: return@GeofenceAttendanceContent
            haptics.tapHeavy()
            viewModel.submitGpsCheckin(fix.latitude, fix.longitude, fix.accuracyMeters, fix.isMock)
        },
        onDismissError = viewModel::clearError,
        onNavigateBack = onNavigateBack,
    )
}

/** The check-in screen without state of its own, for previews and screenshots. */
@Composable
fun GeofenceAttendanceContent(
    location: LocationState,
    status: GeofenceStatus,
    radiusMeters: Double,
    submitting: Boolean,
    result: AttendanceCheckinResponse?,
    queuedOffline: Boolean,
    errorMessage: String?,
    onAllowLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshLocation: () -> Unit,
    onCheckIn: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val done = result != null || queuedOffline
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = "Presensi GPS", subtitle = "Masuk sekolah dengan lokasi HP", onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                when {
                    result != null -> InlineBanner(
                        title = "Presensi tercatat",
                        message = listOfNotNull(result.checkinTime.takeIf { it.isNotBlank() }, statusLabel(result.statusType))
                            .joinToString(" · ").ifEmpty { result.message },
                        tone = if (result.statusType.equals("TERLAMBAT", ignoreCase = true)) StatusTone.Warning else StatusTone.Success,
                        modifier = Modifier.testTag("geofence_result"),
                    )
                    queuedOffline -> InlineBanner(
                        title = "Belum terkirim",
                        message = "Presensi disimpan di HP dan dikirim otomatis saat kembali online.",
                        tone = StatusTone.Info,
                    )
                }
                if (errorMessage != null) {
                    InlineBanner(
                        title = "Presensi ditolak",
                        message = errorMessage,
                        tone = StatusTone.Danger,
                        onDismiss = onDismissError,
                    )
                }

                StatusCard(location, status)

                Text(
                    "Radius presensi sekolah: ${GeofenceStatus.formatDistance(radiusMeters)} (ditetapkan sekolah).",
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )

                when (location) {
                    LocationState.NeedsPermission -> SistaButton(
                        "Izinkan akses lokasi",
                        onAllowLocation,
                        size = ButtonSize.Large,
                        leadingIcon = Icons.Outlined.MyLocation,
                        fullWidth = true,
                    )
                    LocationState.PermissionDenied -> SistaButton(
                        "Buka Pengaturan",
                        onOpenSettings,
                        size = ButtonSize.Large,
                        leadingIcon = Icons.Outlined.Settings,
                        fullWidth = true,
                    )
                    else -> {
                        SistaButton(
                            text = if (done) "Presensi sudah dikirim" else "Presensi Sekarang",
                            onClick = onCheckIn,
                            size = ButtonSize.Large,
                            fullWidth = true,
                            loading = submitting,
                            enabled = status.canCheckIn && !done,
                            modifier = Modifier.testTag("geofence_checkin_button"),
                        )
                        SistaButton(
                            text = "Perbarui lokasi",
                            onClick = onRefreshLocation,
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Large,
                            leadingIcon = Icons.Outlined.MyLocation,
                            fullWidth = true,
                            enabled = location != LocationState.Locating && !submitting,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(location: LocationState, status: GeofenceStatus) {
    val tone = when (status.kind) {
        GeofenceStatus.Kind.SUCCESS -> StatusTone.Success
        GeofenceStatus.Kind.WARNING -> StatusTone.Warning
        GeofenceStatus.Kind.DANGER -> StatusTone.Danger
        GeofenceStatus.Kind.INFO -> StatusTone.Info
    }
    val icon = when {
        location == LocationState.PermissionDenied || location == LocationState.NeedsPermission -> Icons.Outlined.LocationOff
        location == LocationState.Unavailable -> Icons.Outlined.GpsOff
        status.kind == GeofenceStatus.Kind.SUCCESS -> Icons.Outlined.CheckCircle
        status.kind == GeofenceStatus.Kind.INFO -> Icons.Outlined.LocationOn
        else -> Icons.Outlined.WarningAmber
    }
    SistaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("geofence_status")
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (location == LocationState.Locating) {
                CircularProgressIndicator(Modifier.size(40.dp).padding(Spacing.xs), strokeWidth = 3.dp)
            } else {
                IconBadge(icon, tone = tone, size = 40.dp)
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(status.title, style = SistaTheme.typography.titleMedium, color = SistaTheme.colors.onSurface)
                if (status.body.isNotBlank()) {
                    Text(status.body, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant)
                }
            }
        }
    }
}

private fun statusLabel(statusType: String): String? = when (statusType.uppercase()) {
    "HADIR" -> "Hadir"
    "TERLAMBAT" -> "Terlambat"
    "" -> null
    else -> statusType.lowercase().replaceFirstChar { it.uppercase() }
}
