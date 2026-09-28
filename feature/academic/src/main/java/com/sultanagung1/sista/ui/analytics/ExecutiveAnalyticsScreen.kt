package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.AttendanceBreakdown
import com.sultanagung1.sista.data.model.AttendanceDay
import com.sultanagung1.sista.data.model.ExecutiveAnalyticsData
import com.sultanagung1.sista.data.model.ExecutiveSpp

/**
 * Leadership analytics: today's attendance by status, the attendance of the
 * last 14 days, and how much of this month's SPP is settled. Every figure
 * comes from `analytics/executive/kpi`; a missing figure reads "–".
 */
@Composable
fun ExecutiveAnalyticsScreen(
    viewModel: ExecutiveAnalyticsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) refreshRequested = false }

    ExecutiveAnalyticsContent(
        state = state,
        refreshing = refreshRequested && state.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.load()
        },
        onRetry = viewModel::load,
        onNavigateBack = onNavigateBack,
    )
}

/** The executive analytics screen without a ViewModel, for previews and screenshots. */
@Composable
fun ExecutiveAnalyticsContent(
    state: ExecutiveUiState,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = "Analitik eksekutif", subtitle = "Kehadiran dan SPP", onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("executive_analytics_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    val data = state.data
                    when {
                        data == null && state.errorMessage != null -> item(key = "error") {
                            ErrorState(title = "Analitik belum bisa dimuat", body = state.errorMessage, onRetry = onRetry)
                        }
                        data == null -> item(key = "loading") { SkeletonList(rows = 4) }
                        else -> {
                            state.errorMessage?.let { message ->
                                item(key = "stale") {
                                    InlineBanner(message = "Data belum diperbarui. $message", tone = StatusTone.Warning)
                                }
                            }
                            item(key = "overview") { Overview(data) }
                            // An older server sends neither part; saying "nothing recorded" would be wrong.
                            if (data.attendance == null || data.spp == null) {
                                item(key = "unsupported") {
                                    InlineBanner(
                                        message = "Server belum mengirim rincian kehadiran dan SPP. Minta admin memperbarui server.",
                                        tone = StatusTone.Info,
                                    )
                                }
                            }
                            data.attendance?.let { attendance ->
                                item(key = "today_header") { SectionHeader("Kehadiran hari ini") }
                                item(key = "today") { TodayAttendance(attendance.today) }
                                item(key = "trend_header") { SectionHeader("Kehadiran 14 hari terakhir") }
                                item(key = "trend") { AttendanceTrend(attendance.days) }
                            }
                            data.spp?.let { spp ->
                                item(key = "spp_header") { SectionHeader(spp.month.takeIf { it.isNotBlank() }?.let { "SPP $it" } ?: "SPP bulan ini") }
                                item(key = "spp") { SppSettlement(spp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Overview(data: ExecutiveAnalyticsData) {
    val spp = data.spp
    val change = spp?.receivedChangePct
    Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        StatTile(
            label = "Siswa aktif",
            value = thousands(data.totalActiveStudents),
            supporting = "seluruh jenjang",
            icon = Icons.Outlined.Groups,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            label = "SPP diterima",
            value = spp?.let { rupiahShort(it.receivedThisMonth) } ?: "–",
            supporting = when {
                spp == null -> "belum ada data"
                change == null -> "bulan ini"
                else -> "${signed(change)}% dari bulan lalu"
            },
            icon = Icons.Outlined.Payments,
            tone = when {
                change == null -> StatusTone.Neutral
                change >= 0 -> StatusTone.Success
                else -> StatusTone.Warning
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

private data class StatusSlice(val label: String, val count: Int, val tone: StatusTone)

@Composable
private fun TodayAttendance(today: AttendanceBreakdown?) {
    if (today == null || today.recorded == 0) {
        EmptyState(
            title = "Belum ada presensi hari ini",
            body = "Angka muncul setelah guru mencatat presensi kelas.",
            icon = Icons.Outlined.EventBusy,
        )
        return
    }
    val slices = listOf(
        StatusSlice("Hadir", today.present, StatusTone.Success),
        StatusSlice("Sakit", today.sick, StatusTone.Info),
        StatusSlice("Izin", today.permit, StatusTone.Warning),
        StatusSlice("Alpa", today.absent, StatusTone.Danger),
    )
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("${decimal(today.rate)}%", style = SistaTheme.typography.headlineMedium)
                Text(
                    "hadir dari ${thousands(today.recorded)} presensi tercatat",
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(SistaTheme.colors.surfaceVariant),
            ) {
                slices.filter { it.count > 0 }.forEach { slice ->
                    Box(
                        Modifier
                            .weight(slice.count.toFloat())
                            .fillMaxHeight()
                            .background(slice.tone.colors().content),
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                slices.forEach { slice ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Dot(slice.tone.colors().content)
                            Text(slice.label, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                        }
                        Text(thousands(slice.count), style = SistaTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun AttendanceTrend(days: List<AttendanceDay>) {
    if (days.isEmpty()) {
        EmptyState(
            title = "Belum ada presensi dalam 14 hari terakhir",
            icon = Icons.Outlined.EventBusy,
        )
        return
    }
    val overall = overallRate(days)
    val lowest = days.minBy { it.rate }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${decimal(overall)}%", style = SistaTheme.typography.titleLarge)
                    Text(
                        "hadir rata-rata, ${days.size} hari tercatat",
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                if (days.size > 1) {
                    StatusPill(text = "Terendah ${shortDay(lowest.date)}", tone = StatusTone.Neutral)
                }
            }
            Row(
                Modifier.fillMaxWidth().height(148.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEach { day -> DayBar(day, isLowest = days.size > 1 && day === lowest, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayBar(day: AttendanceDay, isLowest: Boolean, modifier: Modifier) {
    val fraction = (day.rate / 100).toFloat().coerceIn(0.02f, 1f)
    val color = if (isLowest) StatusTone.Warning.colors().content else SistaTheme.colors.primary
    Column(modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            decimal(day.rate, digits = 0),
            style = SistaTheme.typography.labelSmall,
            color = SistaTheme.colors.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.height(Spacing.xs))
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight(fraction)
                    .clip(SistaTheme.shapes.extraSmall)
                    .background(color),
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        val (weekday, date) = dayParts(day.date)
        Text(weekday, style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant, maxLines = 1, textAlign = TextAlign.Center)
        Text(date, style = SistaTheme.typography.labelSmall, maxLines = 1, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SppSettlement(spp: ExecutiveSpp) {
    if (spp.bills == 0) {
        EmptyState(
            title = "Belum ada tagihan SPP bulan ini",
            body = "Angka pelunasan muncul setelah tagihan bulan ini dibuat.",
            icon = Icons.Outlined.Payments,
        )
        return
    }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("${decimal(spp.settledRate)}%", style = SistaTheme.typography.headlineMedium)
                Text(
                    "terlunasi",
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                )
            }
            LinearProgressIndicator(
                progress = { ((spp.settledRate ?: 0.0) / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = SistaTheme.colors.primary,
                trackColor = SistaTheme.colors.surfaceVariant,
                drawStopIndicator = {},
            )
            Text(
                "${rupiahShort(spp.settled)} dari ${rupiahShort(spp.billed)} yang ditagihkan",
                style = SistaTheme.typography.bodyMedium,
            )
            HorizontalDivider(color = SistaTheme.colors.outlineVariant)
            FigureRow("Tagihan lunas", "${thousands(spp.billsPaid)} dari ${thousands(spp.bills)}")
            FigureRow("Belum lunas", thousands(spp.bills - spp.billsPaid))
            FigureRow("Sisa tagihan", rupiahShort((spp.billed - spp.settled).coerceAtLeast(0.0)))
        }
    }
}

@Composable
private fun FigureRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = SistaTheme.typography.titleSmall)
    }
}
