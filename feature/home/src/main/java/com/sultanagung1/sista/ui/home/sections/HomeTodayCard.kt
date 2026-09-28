package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.ScheduleRules
import com.sultanagung1.sista.data.model.ScheduleRules.LessonStatus

/**
 * Today's lessons from `student/schedule`, in time order, with the one
 * going on (or the next one) marked from the clock. "Tidak ada pelajaran"
 * is only claimed once the server has answered.
 */
@Composable
fun HomeTodayCard(
    todaySchedules: List<ScheduleItem>,
    nowMinutes: Int,
    isLoading: Boolean,
    loadError: String?,
    onRetry: () -> Unit,
    onOpenSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lessons = todaySchedules.sortedBy { ScheduleRules.minutesOf(it.startTime) ?: Int.MAX_VALUE }
    val focus = ScheduleRules.focusOf(lessons, isToday = true, nowMinutes = nowMinutes)

    Column(modifier.padding(horizontal = Spacing.screen)) {
        SectionHeader("Hari ini", actionLabel = "Jadwal lengkap", onAction = onOpenSchedule)
        when {
            todaySchedules.isEmpty() && isLoading -> SkeletonList(rows = 3)
            todaySchedules.isEmpty() && loadError != null -> ErrorState(
                title = "Jadwal belum bisa dimuat",
                body = loadError,
                onRetry = onRetry,
            )
            todaySchedules.isEmpty() -> EmptyState(
                title = "Tidak ada pelajaran hari ini",
                body = "Jadwal minggu ini tetap bisa dilihat di Jadwal lengkap.",
                icon = Icons.Outlined.EventAvailable,
            )
            else -> SistaCard(modifier = Modifier.fillMaxWidth()) {
                lessons.forEachIndexed { index, lesson ->
                    if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                    val status = ScheduleRules.statusOf(lesson, isToday = true, nowMinutes = nowMinutes)
                    TodayLessonRow(
                        lesson = lesson,
                        status = status,
                        isNext = focus?.first?.id == lesson.id && focus.second == LessonStatus.UPCOMING,
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayLessonRow(lesson: ScheduleItem, status: LessonStatus, isNext: Boolean) {
    val start = ScheduleRules.displayTime(lesson.startTime)
    val end = ScheduleRules.displayTime(lesson.endTime)
    val done = status == LessonStatus.DONE
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // The Schedule screen continues this card for the same lesson.
            .sulaoneSharedBounds(key = "schedule_card_${lesson.subjectName}")
            .padding(vertical = Spacing.sm)
            .semantics(mergeDescendants = true) {
                contentDescription = "${lesson.subjectName}, $start sampai $end" + when {
                    status == LessonStatus.ONGOING -> ", sedang berlangsung"
                    isNext -> ", berikutnya"
                    done -> ", selesai"
                    else -> ""
                }
            },
    ) {
        Text(
            start,
            style = SistaTheme.typography.titleSmall,
            color = if (done) SistaTheme.colors.onSurfaceVariant else SistaTheme.colors.onSurface,
            modifier = Modifier.width(52.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                lesson.subjectName,
                style = SistaTheme.typography.bodyLarge,
                color = if (done) SistaTheme.colors.onSurfaceVariant else SistaTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(lesson.teacherName, ScheduleRules.locationOf(lesson)).filter { it.isNotBlank() && it != "—" }.joinToString(" · "),
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        when {
            status == LessonStatus.ONGOING -> StatusPill("Berlangsung", StatusTone.Success)
            isNext -> StatusPill("Berikutnya", StatusTone.Info)
            done -> StatusPill("Selesai", StatusTone.Neutral)
        }
    }
}
