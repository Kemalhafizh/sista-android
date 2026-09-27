package com.sultanagung1.sista.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.util.GeoUtils
import com.sultanagung1.sista.ui.common.OfflineQueuedBanner
import java.util.Locale

/**
 * FASE 67: Overhauled Presensi GPS Geofence Screen.
 * Design-to-code overhaul: Off-white canvas, flat 0dp cards, 0.5dp borders,
 * concentric satellite radar visualizer, and high-contrast WCAG 2.2 AA typography.
 */
@SuppressLint("MissingPermission")
@Composable
fun GeofenceAttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = rememberHapticFeedbackHelper()
    val isDark = MaterialTheme.colorScheme.surface.isDark()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentLatitude by remember { mutableDoubleStateOf(0.0) }
    var currentLongitude by remember { mutableDoubleStateOf(0.0) }
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission.value = granted
    }

    fun fetchCurrentLocation() {
        if (hasLocationPermission.value) {
            isFetchingLocation = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    currentAccuracy = location.accuracy

                    val isMock = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        location.isMock
                    } else {
                        @Suppress("DEPRECATION")
                        location.isFromMockProvider
                    }

                    viewModel.updateCoordinates(location.latitude, location.longitude, isMock)
                }
                isFetchingLocation = false
            }.addOnFailureListener {
                isFetchingLocation = false
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission.value) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            fetchCurrentLocation()
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Presensi GPS Geofence",
                subtitle = "SMA Islam Sultan Agung 1 Semarang",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { msg ->
                    SulaoneErrorBanner(
                        message = msg,
                        onRetry = {
                            haptics.tapLight()
                            viewModel.clearError()
                        }
                    )
                }
            }

            // FASE 69.3: shown instead of the success card when the check-in was
            // queued locally after a true connectivity failure at submit time.
            AnimatedVisibility(visible = uiState.isSuccess && uiState.isQueuedOffline) {
                OfflineQueuedBanner()
            }

            // Success Card
            AnimatedVisibility(visible = uiState.isSuccess && !uiState.isQueuedOffline) {
                uiState.checkinResult?.let { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Slate900 else Emerald50
                        ),
                        border = BorderStroke(0.5.dp, if (isDark) Slate800 else Emerald200),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald600),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.message,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isDark) Emerald300 else Emerald950
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Waktu Absen: ${res.checkinTime} • Status: ${res.statusType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Slate300 else Emerald800
                                )
                            }
                        }
                    }
                }
            }

            // Radar Status Card (Modern Apple HIG / Material 3 Flat Aesthetic)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Slate900 else Color.White
                ),
                border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Chip inside Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Slate800 else Slate100)
                                .border(0.5.dp, if (isDark) Slate700 else Slate200, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Radius Presensi 250m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        LiveStatusChip(
                            text = if (uiState.isInsideRadius) "Aktif" else "Luar Area",
                            color = if (uiState.isInsideRadius) Emerald600 else AccentRose
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Concentric Radar Rings
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.isInsideRadius) Emerald50 else AccentRose.copy(alpha = 0.08f)
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (uiState.isInsideRadius) Emerald200 else AccentRose.copy(alpha = 0.25f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isInsideRadius) Emerald100 else AccentRose.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.isInsideRadius) Emerald600 else AccentRose
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isInsideRadius) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (uiState.isInsideRadius) "Dalam Radius Kampus" else "Di Luar Radius Kampus",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (uiState.isInsideRadius)
                            "Posisi GPS Anda terverifikasi di area SMA Islam Sultan Agung 1 Semarang."
                        else
                            "Jarak Anda: ${String.format(Locale.US, "%.1f", uiState.distanceToCampusMeters)} meter dari gerbang sekolah (Maks: ${GeoUtils.campus.radiusMeters.toInt()}m).",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(
                        color = if (isDark) Slate800 else Slate200,
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // GPS Diagnostic metrics (2-tile Minimalist Bento Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Slate850 else Slate100,
                            border = BorderStroke(0.5.dp, if (isDark) Slate700 else Slate200)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "KOORDINAT GPS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLatitude == 0.0) "Mencari sinyal..." else "${String.format(
                                        Locale.US, "%.4f", currentLatitude)}, ${String.format(Locale.US, "%.4f", currentLongitude)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Slate850 else Slate100,
                            border = BorderStroke(0.5.dp, if (isDark) Slate700 else Slate200)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "AKURASI SENSOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentAccuracy == 0f) "-" else "± ${String.format(
                                        Locale.US, "%.1f", currentAccuracy)} meter",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (currentAccuracy > 0 && currentAccuracy <= 20f) Emerald600 else AccentAmber
                                )
                            }
                        }
                    }
                }
            }

            // Check-in Action Button (Full width, 52dp touch target, spring animation)
            Button(
                onClick = {
                    if (uiState.isInsideRadius) {
                        haptics.tapMedium()
                        viewModel.submitGpsCheckin(
                            lat = currentLatitude,
                            lon = currentLongitude,
                            accuracy = currentAccuracy,
                            isMock = uiState.isMockLocationDetected
                        )
                    } else {
                        haptics.tapLight()
                        fetchCurrentLocation()
                    }
                },
                enabled = !(uiState.isLoading || isFetchingLocation),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isInsideRadius) Emerald600 else Slate700,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    // FASE 74.1: stable automation hook — the GPS check-in flow's
                    // sole entry point has no text/contentDescription that stays
                    // constant across its enabled/loading states, so E2E drivers
                    // (Appium/UiAutomator2) need a fixed testTag to target it.
                    .testTag("geofence_checkin_button")
                    .sulaoneInteractiveTouchTarget(48.dp)
                    .springPressable {
                        if (uiState.isInsideRadius) haptics.tapMedium() else haptics.tapLight()
                    }
            ) {
                if (uiState.isLoading || isFetchingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isFetchingLocation) "Memperbarui GPS..." else "Mengirim Presensi...",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Icon(
                        imageVector = if (uiState.isInsideRadius) Icons.Default.TouchApp else Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isInsideRadius) "Lakukan Presensi Sekarang" else "Perbarui Lokasi GPS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Anti-Cheat Info Notice Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Slate900 else Color.White
                ),
                border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Slate800 else Emerald50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Proteksi Anti-Fake GPS & TEE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Integritas presensi dijamin melalui verifikasi hardware sensor dan pencegahan mock location.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
