package com.sultanagung1.sista.ui.teacher

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.CardVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.JournalScheduleItem

/** The three periods the journal list can show, in tab order (matches `selectedTab`). */
private val PERIODS = listOf(0 to "Hari ini", 1 to "7 hari", 2 to "30 hari")

/**
 * Jurnal mengajar: today's slots from `teacher/schedule` with whether each
 * has a journal in `teacher/journals`, and the journals of the last 7 or
 * 30 days. A filed journal cannot be changed — the server has no update
 * route — so only empty slots offer "Isi jurnal".
 */
@Composable
fun TeachingJournalMobileScreen(
    viewModel: JournalMobileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // The form files a journal with its own ViewModel; reload when coming back to it.
    var started by remember { mutableStateOf(false) }
    LifecycleStartStopEffect(
        onStart = { if (started) viewModel.loadSchedules() else started = true },
        onStop = {},
    )

    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) refreshRequested = false }

    TeachingJournalContent(
        state = uiState,
        refreshing = refreshRequested && uiState.isLoading,
        onSelectPeriod = viewModel::setSelectedTab,
        onRefresh = {
            refreshRequested = true
            viewModel.loadSchedules()
        },
        onFill = { onNavigateToForm(it.id) },
        onNavigateBack = onNavigateBack,
    )
}

/** The journal list without a ViewModel, for previews and screenshots. */
@Composable
fun TeachingJournalContent(
    state: JournalMobileUiState,
    refreshing: Boolean,
    onSelectPeriod: (Int) -> Unit,
    onRefresh: () -> Unit,
    onFill: (JournalScheduleItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val items = state.currentTabSchedules
    val today = state.selectedTab == 0
    val hasAnyData = state.todaySchedule.isNotEmpty() || state.weekJournals.isNotEmpty() || state.monthJournals.isNotEmpty()
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = "Jurnal mengajar", onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    item(key = "periods") {
                        FilterChipRow(
                            options = PERIODS,
                            selected = PERIODS[state.selectedTab.coerceIn(0, 2)],
                            onSelect = { onSelectPeriod(it.first) },
                            label = { it.second },
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                    if (today && state.todaySchedule.isNotEmpty()) {
                        item(key = "progress") { TodayProgress(state, Modifier.padding(horizontal = Spacing.screen)) }
                    }
                    if (state.errorMessage != null && hasAnyData) {
                        item(key = "stale") {
                            InlineBanner(
                                message = "Gagal memperbarui. ${state.errorMessage}",
                                tone = StatusTone.Warning,
                                actionLabel = "Coba lagi",
                                onAction = onRefresh,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    when {
                        state.isLoading && !hasAnyData -> item(key = "loading") { SkeletonList(rows = 4) }
                        state.errorMessage != null && !hasAnyData -> item(key = "error") {
                            ErrorState(
                                title = "Jurnal belum bisa dimuat",
                                body = state.errorMessage,
                                onRetry = onRefresh,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                        items.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                title = if (today) "Tidak ada jadwal mengajar hari ini" else "Belum ada jurnal pada periode ini",
                                body = if (today) "Jurnal diisi per jam pelajaran sesuai jadwal Anda." else "Jurnal yang Anda kirim akan muncul di sini.",
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                        else -> items(items, key = { it.id }, contentType = { "journal" }) { item ->
                            JournalCard(
                                item = item,
                                showDate = !today,
                                onFill = { onFill(item) },
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayProgress(state: JournalMobileUiState, modifier: Modifier = Modifier) {
    val c = state.compliance
    val missing = c.totalScheduled - c.filledCount
    SistaCard(modifier = modifier.fillMaxWidth(), variant = CardVariant.Outlined) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Jurnal hari ini", style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
                    Text("${c.filledCount} dari ${c.totalScheduled} jam terisi", style = SistaTheme.typography.titleMedium)
                }
                if (missing == 0) {
                    StatusPill("Lengkap", StatusTone.Success)
                } else {
                    StatusPill("$missing belum diisi", StatusTone.Warning)
                }
            }
            LinearProgressIndicator(
                progress = { if (c.totalScheduled == 0) 0f else c.filledCount / c.totalScheduled.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(SistaTheme.shapes.small),
                color = SistaTheme.colors.primary,
                trackColor = SistaTheme.colors.surfaceVariant,
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun JournalCard(item: JournalScheduleItem, showDate: Boolean, onFill: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(item.id) { mutableStateOf(false) }
    val (label, tone) = journalState(item)
    SistaCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics { if (item.isFilled) stateDescription = if (expanded) "Rincian terbuka" else "Rincian tertutup" },
        onClick = if (item.isFilled) ({ expanded = !expanded }) else onFill,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.subject, style = SistaTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOfNotNull(
                            item.date?.takeIf { showDate }?.let(::journalDate),
                            "Kelas ${item.className}",
                            item.timeSlot,
                        ).joinToString(" · "),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                StatusPill(label, tone)
            }
            if (item.isFilled) {
                Text(
                    item.topic?.takeIf { it.isNotBlank() } ?: "Tanpa materi",
                    style = SistaTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (expanded) JournalDetails(item)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Hadir ${item.attendancePresent ?: "–"} · Tidak hadir ${item.attendanceAbsent ?: "–"}",
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            } else {
                SistaButton("Isi jurnal", onFill, leadingIcon = Icons.Outlined.EditNote, fullWidth = true, variant = ButtonVariant.Primary)
            }
        }
    }
}

@Composable
private fun JournalDetails(item: JournalScheduleItem) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
        Detail("Metode", item.method)
        Detail("Kegiatan pembelajaran", item.media)
        Detail("Kendala", item.notes)
        Detail("Catatan & tindak lanjut", item.followUp)
    }
}

@Composable
private fun Detail(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column {
        Text(label, style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
        Text(value, style = SistaTheme.typography.bodyMedium)
    }
}

/** The pill: whether today's slot is still empty, else the journal's review status. */
private fun journalState(item: JournalScheduleItem): Pair<String, StatusTone> =
    if (!item.isFilled) "Belum diisi" to StatusTone.Warning else journalReviewStatus(item.status)

/**
 * `teaching_journals.status` is draft → submitted → reviewed. The app files
 * drafts; submitting for review happens in the web's Jurnal Mengajar.
 */
internal fun journalReviewStatus(status: String?): Pair<String, StatusTone> = when (status?.lowercase()) {
    "draft" -> "Draf" to StatusTone.Warning
    "submitted" -> "Menunggu review" to StatusTone.Info
    "reviewed" -> "Sudah direview" to StatusTone.Success
    else -> "Terisi" to StatusTone.Neutral
}

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

/** "2026-09-25" → "25 Sep". */
internal fun journalDate(iso: String): String {
    val parts = iso.take(10).split("-")
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { MONTHS.getOrNull(it - 1) }
    val day = parts.getOrNull(2)?.toIntOrNull()
    return if (month != null && day != null) "$day $month" else iso
}
