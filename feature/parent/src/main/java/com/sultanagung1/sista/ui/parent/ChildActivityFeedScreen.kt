package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ChildActivityEvent

/**
 * The chosen child's school events of the last 14 days
 * (`parent/child/{uuid}/feed`), grouped by day, newest first.
 */
@Composable
fun ChildActivityFeedScreen(
    studentUuid: String?,
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    // This screen has its own ViewModel; follow the child it was opened for.
    LaunchedEffect(studentUuid) { viewModel.focusChild(studentUuid) }
    val now = remember(uiState.activityFeed) { DateUtils.nowMillis() }

    ChildActivityFeedContent(
        childName = uiState.selectedChild?.name,
        events = uiState.activityFeed,
        nowMillis = now,
        loading = uiState.isLoading || uiState.isLoadingExperience,
        errorMessage = uiState.errorMessage ?: uiState.childErrorMessage,
        onRetry = viewModel::refresh,
        onNavigateBack = onNavigateBack,
    )
}

/** The feed without a ViewModel, for previews and screenshots. */
@Composable
fun ChildActivityFeedContent(
    childName: String?,
    events: List<ChildActivityEvent>,
    nowMillis: Long,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    // Events arrive newest first; keep that order within and across days.
    val days = remember(events, nowMillis) { events.groupBy { dayLabel(it.timestamp, nowMillis) }.toList() }
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = "Aktivitas", subtitle = childName?.let { "$it · 14 hari terakhir" }, onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                when {
                    loading && events.isEmpty() -> item(key = "loading") { SkeletonList(rows = 6) }
                    errorMessage != null && events.isEmpty() -> item(key = "error") {
                        ErrorState(title = "Aktivitas belum bisa dimuat", body = errorMessage, onRetry = onRetry)
                    }
                    events.isEmpty() -> item(key = "empty") {
                        EmptyState(
                            title = "Belum ada aktivitas",
                            body = "Presensi, nilai, poin, perpustakaan, dan setoran tahfidz 14 hari terakhir muncul di sini.",
                            icon = Icons.Outlined.Timeline,
                        )
                    }
                    else -> days.forEach { (day, dayEvents) ->
                        item(key = "day_$day", contentType = "day") {
                            Column {
                                SectionHeader(day)
                                SistaCard(modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        dayEvents.forEachIndexed { index, event ->
                                            if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                                            ActivityRow(event, nowMillis, showDay = false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
