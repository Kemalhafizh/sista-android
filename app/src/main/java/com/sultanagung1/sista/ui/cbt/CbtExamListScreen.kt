package com.sultanagung1.sista.ui.cbt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.data.model.CbtExamItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * FASE 67: Overhauled CBT Exam List Screen.
 * Design-to-code overhaul: Off-white canvas (Slate50), flat 0dp cards,
 * 0.5dp borders, Emerald600 accents, and 48dp WCAG touch targets.
 */
@Composable
fun CbtExamListScreen(
    viewModel: CbtViewModel,
    onNavigateToRoom: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    val sampleExams = listOf(
        CbtExamItem(101, "Penilaian Harian Bersama 1 (PHB 1)", "Matematika Peminatan", 90, 30, "08:00", "09:30", "ACTIVE"),
        CbtExamItem(102, "Try Out Ujian Masuk PTN 2027", "Tes Potensi Skolastik (TPS)", 120, 45, "10:00", "12:00", "UPCOMING"),
        CbtExamItem(103, "Ujian Tengah Semester Ganjil", "Pendidikan Agama Islam", 60, 25, "13:00", "14:00", "UPCOMING")
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Ujian Berbasis Komputer (CBT)",
                subtitle = "Sistem Ujian Terintegrasi Anti-Cheat",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        var isRefreshing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        SulaonePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.loadExams()
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info / Security Protocol Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Slate850 else Color.White
                        ),
                        border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Slate800 else Emerald50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Protokol Keamanan Ujian",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Slate900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Keluar dari layar ujian atau membuka aplikasi lain otomatis mendiskualifikasi sesi dan dilaporkan ke pengawas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Slate400 else Slate500,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                val displayList = if (uiState.exams.isEmpty()) sampleExams else uiState.exams

                items(displayList) { item ->
                    val isActive = item.status == "ACTIVE"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sulaoneSharedBounds(key = "cbt_exam_card_${item.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Slate850 else Color.White
                        ),
                        border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.subject,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Emerald300 else Emerald600,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isActive) {
                                        if (isDark) Slate800 else Emerald50
                                    } else {
                                        if (isDark) Slate800 else Slate100
                                    },
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (isActive) Emerald200 else (if (isDark) Slate700 else Slate200)
                                    )
                                ) {
                                    Text(
                                        text = if (isActive) "AKTIF SEKARANG" else "AKAN DATANG",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) (if (isDark) Emerald300 else Emerald700) else (if (isDark) Slate400 else Slate500),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Slate900
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (isDark) Slate400 else Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${item.durationMinutes} Menit • ${item.totalQuestions} Soal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Slate400 else Slate500
                                    )
                                }

                                Button(
                                    onClick = {
                                        haptics.tapLight()
                                        onNavigateToRoom(item.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) Emerald600 else Slate700,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp),
                                    modifier = Modifier
                                        .sulaoneInteractiveTouchTarget(48.dp)
                                        .springPressable()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isActive) "Mulai Ujian" else "Siap",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
