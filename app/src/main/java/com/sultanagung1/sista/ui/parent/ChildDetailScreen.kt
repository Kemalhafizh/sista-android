package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun ChildDetailScreen(
    studentId: String,
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ChildDetailScreen gets its own ParentViewModel instance (separate nav
    // back-stack entry from the dashboard), so it must re-select the child
    // named by [studentId] once that fresh instance's children list loads —
    // otherwise it silently shows whichever child loaded first.
    LaunchedEffect(uiState.children, studentId) {
        val match = uiState.children.firstOrNull { it.uuid == studentId }
        if (match != null && uiState.selectedChild?.uuid != studentId) {
            viewModel.selectChild(match)
        }
    }

    val child = uiState.selectedChild
    val grades = uiState.childGrades
    val logs = uiState.childAttendanceLogs

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Rapor & Nilai", "Riwayat Presensi")

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = child?.name ?: "Detail Perkembangan Ananda",
                subtitle = "${child?.classroom ?: "-"} • NISN: ${child?.nisn ?: "-"}",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald800
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (grades.isNotEmpty()) {
                            item {
                                val average = grades.map { it.score }.average()
                                SulaoneCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "Rata-rata Nilai", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                            Text(text = String.format("%.1f", average), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Emerald800)
                                        }
                                        SulaoneBadge(text = "${grades.size} Nilai Tercatat", containerColor = Gold100, contentColor = Gold900)
                                    }
                                }
                            }
                        }

                        if (grades.isEmpty()) {
                            item {
                                SulaoneEmptyState(
                                    icon = Icons.Default.School,
                                    title = "Belum Ada Nilai Tercatat",
                                    description = "Nilai akademik ananda akan muncul di sini setelah guru menginput penilaian."
                                )
                            }
                        }

                        items(grades) { grade ->
                            SulaoneCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = grade.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = "${grade.type} • ${grade.date}", style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                                    }
                                    Text(text = grade.score.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Emerald700)
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Text(
                                text = "Log Presensi (30 Hari Terakhir)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (logs.isEmpty()) {
                            item {
                                SulaoneEmptyState(
                                    icon = Icons.Default.EventNote,
                                    title = "Belum Ada Riwayat Presensi",
                                    description = "Riwayat kehadiran ananda akan muncul di sini."
                                )
                            }
                        }

                        items(logs) { log ->
                            SulaoneCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (log.status == "H") Emerald600 else Gold600)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = log.date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            if (!log.notes.isNullOrBlank()) {
                                                Text(text = log.notes, style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    SulaoneBadge(text = log.statusLabel, containerColor = Emerald100, contentColor = Emerald900)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
