package com.sultanagung1.sista.ui.elearning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*

/**
 * Adaptive E-Learning Scaffold yang mengimplementasikan List-Detail Dual-Pane (FASE 55.1).
 * - Smartphone (Compact): Menampilkan daftar kelas LMS, klik berpindah ke detail materi/tugas.
 * - Tablet & Foldable (Medium & Expanded): Menampilkan daftar kelas di pane kiri (360dp)
 *   dan detail ruang kelas/materi di pane kanan secara berdampingan.
 */
@Composable
fun AdaptiveElearningScreen(
    viewModel: ElearningViewModel,
    onNavigateBack: () -> Unit,
    onSelectClass: (Long) -> Unit,
    onNavigateToSubmitAssignment: (Long) -> Unit
) {
    val windowWidthClass = LocalWindowWidthSizeClass.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedClassId by remember { mutableStateOf<Long?>(null) }

    // Auto-select kelas pertama saat di tablet jika belum ada yang dipilih
    LaunchedEffect(windowWidthClass, uiState.classes) {
        if (windowWidthClass != WindowWidthSizeClass.Compact && selectedClassId == null && uiState.classes.isNotEmpty()) {
            selectedClassId = uiState.classes.first().id
        }
    }

    if (windowWidthClass == WindowWidthSizeClass.Compact) {
        // Mode Smartphone: Navigasi Single Pane
        ElearningClassListScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onSelectClass = onSelectClass
        )
    } else {
        // Mode Tablet & Foldable: Dual-Pane List-Detail Scaffold
        SulaoneListDetailPaneScaffold(
            selectedItem = selectedClassId ?: 0L,
            onSelectItem = { selectedClassId = it },
            listPaneWidth = 360.dp,
            listPane = {
                ElearningClassListScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onSelectClass = { classId ->
                        selectedClassId = classId
                    }
                )
            },
            detailPane = { classId, _ ->
                if (classId != null && classId > 0L) {
                    ElearningClassDetailScreen(
                        classId = classId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            selectedClassId = null
                        },
                        onNavigateToSubmitAssignment = onNavigateToSubmitAssignment
                    )
                } else {
                    EmptyElearningPlaceholder()
                }
            },
            emptyDetailPlaceholder = {
                EmptyElearningPlaceholder()
            }
        )
    }
}

@Composable
private fun EmptyElearningPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pilih Ruang Kelas E-Learning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pilih mata pelajaran atau kelas digital di sebelah kiri untuk melihat materi pembelajaran, slide, video, dan tugas terstruktur.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 380.dp)
            )
        }
    }
}
