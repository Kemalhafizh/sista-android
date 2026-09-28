package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.SubjectAverage

/** Top bar, pull-to-refresh and a padded list: the frame of every analytics screen. */
@Composable
internal fun AnalyticsScaffold(
    title: String,
    subtitle: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    testTag: String,
    content: LazyListScope.() -> Unit,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = title, subtitle = subtitle, onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag(testTag),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    content = content,
                )
            }
        }
    }
}

/** A subject's average as a bar out of 100, with a tick where the KKM is. */
@Composable
internal fun SubjectBar(subject: SubjectAverage, modifier: Modifier = Modifier) {
    val below = subject.average < subject.kkm
    val barColor = if (below) StatusTone.Danger.colors().content else SistaTheme.colors.primary
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(subject.name, style = SistaTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1)
            if (below) StatusPill(text = "Di bawah KKM", tone = StatusTone.Danger)
            Text(decimal(subject.average), style = SistaTheme.typography.titleMedium, color = if (below) barColor else SistaTheme.colors.onSurface)
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SistaTheme.colors.surfaceVariant),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((subject.average / 100).toFloat().coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(barColor),
                )
            }
            Box(
                Modifier
                    .offset(x = maxWidth * (subject.kkm / 100).toFloat().coerceIn(0f, 1f) - 1.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(SistaTheme.colors.onSurface),
            )
        }
        Text(
            buildString {
                append("KKM ${decimal(subject.kkm)}")
                if (subject.gradesCount > 0) append(" · ${subject.gradesCount} nilai")
            },
            style = SistaTheme.typography.bodySmall,
            color = SistaTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun DayMark.color(): Color = when (this) {
    DayMark.Present -> StatusTone.Success.colors().content
    DayMark.Sick -> StatusTone.Info.colors().content
    DayMark.Permit -> StatusTone.Warning.colors().content
    DayMark.Absent -> StatusTone.Danger.colors().content
    DayMark.Unrecorded -> SistaTheme.colors.surfaceVariant
}

private val WEEK_HEADER = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

/** The month's attendance as a Monday-first calendar. */
@Composable
internal fun AttendanceMonth(weeks: List<List<MonthCell?>>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            WEEK_HEADER.forEach {
                Text(
                    it,
                    style = SistaTheme.typography.labelSmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                week.forEach { cell ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (cell != null) {
                            val mark = cell.mark
                            val filled = mark != null && mark != DayMark.Unrecorded
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(SistaTheme.shapes.extraSmall)
                                    .background(
                                        when {
                                            mark == null -> Color.Transparent
                                            else -> mark.color()
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    cell.day.toString(),
                                    style = SistaTheme.typography.labelMedium,
                                    color = when {
                                        filled -> SistaTheme.colors.surface
                                        mark == null -> SistaTheme.colors.outline
                                        else -> SistaTheme.colors.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Legend(mark: DayMark, count: Int, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(mark.color()),
            )
            Text(mark.label, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant, maxLines = 1)
        }
        Text(count.toString(), style = SistaTheme.typography.titleMedium)
    }
}
