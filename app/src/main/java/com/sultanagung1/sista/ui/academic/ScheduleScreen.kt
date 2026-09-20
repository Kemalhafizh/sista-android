/**
 * FASE 67
 */
package com.sultanagung1.sista.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.data.model.ScheduleItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen(
    viewModel: AcademicViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()
    val isDark = isSystemInDarkTheme()

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")

    val sampleSchedules = listOf(
        ScheduleItem(1, "Senin", "07:00", "08:30", "Matematika Peminatan", "Drs. H. Ahmad Fauzi, M.Pd", "Lab Komputer 2"),
        ScheduleItem(2, "Senin", "08:30", "10:00", "Fisika Modern", "Dr. Hj. Siti Nurjanah, M.Si", "Lab Fisika 1"),
        ScheduleItem(3, "Senin", "10:15", "11:45", "Pendidikan Agama Islam", "Ust. M. Rizqi, Lc., M.Hum", "Masjid Sultan Agung Lt. 2"),
        ScheduleItem(4, "Senin", "12:30", "14:00", "Bahasa Inggris Lanjutan", "Sarah Jenkins, M.Ed", "Kelas XII MIPA 1")
    )

    val displayList = if (uiState.allSchedules.isEmpty()) sampleSchedules else uiState.allSchedules
    
    val borderColor = if (isDark) Slate800 else Slate200
    val backgroundColor = if (isDark) MaterialTheme.colorScheme.background else Slate50
    val textMain = if (isDark) Slate50 else Slate900
    val textSub = if (isDark) Slate400 else Slate500
    val textMuted = if (isDark) Slate500 else Slate400

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Jadwal Pelajaran",
                subtitle = "Semester Ganjil 2026/2027",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            // Section Header Summary Card
            ModernBentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (isDark) MaterialTheme.colorScheme.surface else Slate50,
                borderColor = borderColor,
                elevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Senin, 20 September 2026",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${displayList.size} Pelajaran hari ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSub
                        )
                    }
                    SulaoneBadge(
                        text = "XII MIPA 1",
                        containerColor = Emerald600,
                        contentColor = Slate50
                    )
                }
            }

            // Day selector tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { day ->
                    val isSelected = uiState.selectedDay == day
                    val shortDay = day.take(3).uppercase()
                    
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) Emerald600 else Color.Transparent)
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) Emerald600 else borderColor,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .springPressable {
                                haptics.tapLight()
                                viewModel.selectDay(day)
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = shortDay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Slate50 else textSub
                            )
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Slate50 else if (isDark) Slate400 else Slate600
                            )
                        }
                    }
                }
            }

            var isRefreshing by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            SulaonePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        viewModel.selectDay(uiState.selectedDay)
                        delay(600)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(displayList) { item ->
                        val isFirstItem = displayList.firstOrNull() == item
                        val cardBg = if (isFirstItem) {
                            if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50
                        } else {
                            if (isDark) MaterialTheme.colorScheme.surface else Slate50
                        }

                        ModernBentoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sulaoneSharedBounds(key = "schedule_card_${item.subjectName}"),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = cardBg,
                            borderColor = borderColor,
                            elevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Lesson number indicator
                                Text(
                                    text = item.id.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFirstItem) Emerald600 else textMuted,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                // Time Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isDark) Slate800 else Slate200)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = item.startTime,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = textMain
                                        )
                                        Text(
                                            text = item.endTime,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = textSub
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.subjectName,
                                            modifier = Modifier.weight(1f, fill = false),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = textMain,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isFirstItem) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            LiveStatusChip("Berlangsung", color = Emerald600)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = textSub,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = item.teacherName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSub,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Room,
                                            contentDescription = null,
                                            tint = textMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = item.room,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = textMuted
                                        )
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
