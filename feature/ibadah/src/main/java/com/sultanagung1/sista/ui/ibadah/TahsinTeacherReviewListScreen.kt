package com.sultanagung1.sista.ui.ibadah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*

/**
 * Teacher's queue of Tahsin recordings routed to them (their homeroom
 * students) — the counterpart to the student's TahsinRecorderScreen, closing
 * the "guru menyimak serta memberi feedback" loop from the FASE 72.1 spec.
 */
@Composable
fun TahsinTeacherReviewListScreen(
    viewModel: TahsinViewModel,
    onOpenSubmission: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = pending, 1 = reviewed

    LaunchedEffect(selectedTab) {
        viewModel.loadAssignedSubmissions(if (selectedTab == 0) "pending" else "reviewed")
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Evaluasi Setoran Tahsin",
                subtitle = "Simak & Beri Catatan Tajwid Siswa",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Menunggu") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Selesai") })
            }

            when {
                uiState.isLoadingList -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald600)
                }
                uiState.listErrorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SulaoneErrorBanner(
                        message = uiState.listErrorMessage ?: "Gagal memuat daftar setoran",
                        onRetry = { viewModel.loadAssignedSubmissions(if (selectedTab == 0) "pending" else "reviewed") },
                        modifier = Modifier.padding(24.dp)
                    )
                }
                uiState.submissions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 0) "Tidak ada setoran yang menunggu evaluasi." else "Belum ada setoran yang selesai dievaluasi.",
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
                        TahsinSubmissionRow(
                            submission = submission,
                            onClick = { onOpenSubmission(submission.id) },
                            showStudentName = true
                        )
                    }
                }
            }
        }
    }
}
