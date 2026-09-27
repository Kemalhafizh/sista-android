package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.TeacherCbtExamItem
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * "Pengawas CBT" entry point. Shows only exams the backend says this teacher
 * operates; if exactly one is ongoing it opens that exam's proctor screen
 * directly (replacePicker = true). Never invents an exam id.
 */
@Composable
fun TeacherProctorExamsScreen(
    viewModel: TeacherProctorExamsViewModel = hiltViewModel(),
    onOpenExam: (examId: Long, replacePicker: Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.autoOpenExamId) {
        uiState.autoOpenExamId?.let { examId ->
            viewModel.onAutoOpenConsumed()
            onOpenExam(examId, true)
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pengawas CBT",
                subtitle = "Pilih ujian yang Anda awasi",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        SulaonePullToRefreshBox(
            isRefreshing = uiState.isLoading && uiState.hasLoaded,
            onRefresh = { viewModel.loadExams() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    item {
                        SulaoneErrorBanner(message = message, onRetry = { viewModel.loadExams() })
                    }
                }

                when {
                    !uiState.hasLoaded -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Emerald600)
                        }
                    }
                    uiState.exams.isEmpty() && uiState.errorMessage == null -> item {
                        SulaoneEmptyState(
                            icon = Icons.Default.EventBusy,
                            title = "Belum Ada Ujian untuk Diawasi",
                            description = "Tidak ada ujian terbit yang sedang berlangsung atau terjadwal atas nama Anda. " +
                                "UTS/UAS muncul di sini jika Anda ditunjuk sebagai Operator Ujian; " +
                                "Ulangan Harian/Try Out muncul jika Anda pembuatnya.",
                            ctaLabel = "Muat Ulang",
                            onCtaClick = { viewModel.loadExams() }
                        )
                    }
                    else -> items(uiState.exams, key = { it.id }) { exam ->
                        ProctorExamCard(exam = exam, onClick = { onOpenExam(exam.id, false) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProctorExamCard(exam: TeacherCbtExamItem, onClick: () -> Unit) {
    val accent = if (exam.isOngoing) Emerald700 else Slate500
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, Slate200),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(examTypeLabel(exam.type), exam.subject, exam.classroom).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (exam.isOngoing) {
                    SulaoneBadge(text = "Sedang Berlangsung")
                } else {
                    Text(
                        text = "Mulai ${formatExamTime(exam.startTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
        }
    }
}

private fun examTypeLabel(type: String): String = when (type) {
    "uts" -> "UTS"
    "uas" -> "UAS"
    "ulangan_harian" -> "Ulangan Harian"
    "try_out" -> "Try Out"
    else -> type
}

private val examTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy • HH:mm", Locale("id", "ID"))

private fun formatExamTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(examTimeFormatter)
    } catch (e: Exception) {
        iso
    }
}
