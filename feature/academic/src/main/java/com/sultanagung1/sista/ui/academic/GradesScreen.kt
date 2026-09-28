package com.sultanagung1.sista.ui.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.GradeEntry
import com.sultanagung1.sista.data.model.GradeRules

/**
 * The student's grades from `student/grades`, per subject, newest first,
 * with a filter per assessment type. Only what teachers entered is shown:
 * no pass/fail colours, because the minimum (KKTP) differs per subject and
 * the server does not send it.
 */
@Composable
fun GradesScreen(
    viewModel: AcademicViewModel,
    onNavigateBack: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    // The spinner stays until the server has actually answered.
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isGradesLoading) {
        if (!uiState.isGradesLoading) refreshRequested = false
    }

    GradesContent(
        grades = uiState.grades,
        loading = uiState.isGradesLoading,
        errorMessage = uiState.gradesErrorMessage,
        refreshing = refreshRequested && uiState.isGradesLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.loadGrades()
        },
        onNavigateBack = onNavigateBack,
    )
}

/** The grade list without state of its own (apart from the type filter), for previews and screenshots. */
@Composable
fun GradesContent(
    grades: List<GradeEntry>,
    loading: Boolean,
    errorMessage: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    val types = GradeRules.typesOf(grades)
    val shown = GradeRules.filter(grades, selectedType)
    val subjects = GradeRules.bySubject(shown)
    val average = GradeRules.overallAverage(shown)

    ShellTheme {
        // The bar takes a surface tone once the list scrolls beneath it.
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SistaTopBar(
                    title = "Nilai",
                    subtitle = "Dari nilai yang diinput guru",
                    onBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                )
            },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("grades_list"),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    when {
                        grades.isEmpty() && loading -> item { SkeletonList(rows = 4) }
                        grades.isEmpty() && errorMessage != null -> item {
                            ErrorState(title = "Nilai belum bisa dimuat", body = errorMessage, onRetry = onRefresh)
                        }
                        grades.isEmpty() -> item {
                            EmptyState(
                                title = "Belum ada nilai",
                                body = "Nilai akan muncul di sini setelah guru memasukkannya.",
                                icon = Icons.Outlined.Grade,
                            )
                        }
                        else -> {
                            if (errorMessage != null) {
                                item {
                                    InlineBanner(
                                        message = "Menampilkan nilai tersimpan. $errorMessage",
                                        tone = StatusTone.Warning,
                                        actionLabel = "Muat ulang",
                                        onAction = onRefresh,
                                    )
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                    StatTile(
                                        label = "Rata-rata",
                                        value = average?.let(GradeRules::formatScore) ?: "–",
                                        supporting = "dari ${shown.size} nilai",
                                        icon = Icons.Outlined.Insights,
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatTile(
                                        label = "Mata pelajaran",
                                        value = subjects.size.toString(),
                                        supporting = if (selectedType == null) "semua penilaian" else "penilaian $selectedType",
                                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                            if (types.size > 1) {
                                item {
                                    FilterChipRow(
                                        options = listOf<String?>(null) + types,
                                        selected = selectedType,
                                        onSelect = { selectedType = it },
                                        label = { it ?: "Semua" },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.testTag("grades_type_filter"),
                                    )
                                }
                            }
                            items(subjects, key = { it.subject }) { subject -> SubjectCard(subject) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(subject: GradeRules.SubjectGrades) {
    val average = GradeRules.formatScore(subject.average)
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = "${subject.subject}, rata-rata $average dari ${subject.entries.size} nilai"
            },
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    subject.subject,
                    style = SistaTheme.typography.titleMedium,
                    color = SistaTheme.colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${subject.entries.size} nilai",
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(average, style = SistaTheme.typography.headlineSmall, color = SistaTheme.colors.primary)
                Text("rata-rata", style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(Spacing.md))
        subject.entries.forEachIndexed { index, entry ->
            if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
            GradeRow(entry)
        }
    }
}

@Composable
private fun GradeRow(entry: GradeEntry) {
    val score = GradeRules.formatScore(entry.score)
    val date = GradeRules.formatDate(entry.date)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
            .semantics(mergeDescendants = true) {
                contentDescription = "${entry.type} ${entry.description.orEmpty()}, nilai $score, $date"
            },
    ) {
        // A fixed column so descriptions line up whatever the type's length.
        Box(Modifier.widthIn(min = 76.dp)) {
            StatusPill(entry.type.ifBlank { "Nilai" }, StatusTone.Neutral)
        }
        Spacer(Modifier.width(Spacing.sm))
        Column(Modifier.weight(1f)) {
            val description = entry.description?.takeIf { it.isNotBlank() }
            if (description != null) {
                Text(description, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(date, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
        }
        Text(score, style = SistaTheme.typography.titleMedium, color = SistaTheme.colors.onSurface)
    }
}
