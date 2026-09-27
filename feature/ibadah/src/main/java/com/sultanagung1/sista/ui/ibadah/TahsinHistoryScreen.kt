package com.sultanagung1.sista.ui.ibadah

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.TahsinSubmissionItem

/**
 * Student's own submission history — the other half of the recorder loop:
 * without this, a student who submits a recording has no way to ever see
 * whether their teacher listened to it or what feedback was left.
 */
@Composable
fun TahsinHistoryScreen(
    viewModel: TahsinViewModel,
    onOpenSubmission: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMySubmissions() }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Riwayat Setoran Tahsin",
                subtitle = "Rekaman & Evaluasi Guru Pembimbing",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoadingList -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald600)
                }
                uiState.listErrorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SulaoneErrorBanner(
                        message = uiState.listErrorMessage ?: "Gagal memuat riwayat setoran",
                        onRetry = { viewModel.loadMySubmissions() },
                        modifier = Modifier.padding(24.dp)
                    )
                }
                uiState.submissions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada setoran tahsin. Rekam bacaan Anda melalui menu Perekam Tahsin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.submissions, key = { it.id }) { submission ->
                        TahsinSubmissionRow(submission = submission, onClick = { onOpenSubmission(submission.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TahsinSubmissionRow(submission: TahsinSubmissionItem, onClick: () -> Unit, showStudentName: Boolean = false) {
    val isReviewed = submission.status == "reviewed"
    SulaoneCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isReviewed) Emerald100 else Gold100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = if (isReviewed) Emerald800 else Gold800
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = submission.surahName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showStudentName && submission.studentName != null) {
                    Text(
                        text = submission.studentName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${submission.annotationCount} catatan tajwid",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SulaoneBadge(
                text = if (isReviewed) "Selesai" else "Menunggu",
                containerColor = if (isReviewed) Emerald100 else Gold100,
                contentColor = if (isReviewed) Emerald800 else Gold800
            )
        }
    }
}
