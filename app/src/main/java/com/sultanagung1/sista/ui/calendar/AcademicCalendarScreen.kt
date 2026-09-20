package com.sultanagung1.sista.ui.calendar

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.designsystem.Emerald700
import com.sultanagung1.sista.core.designsystem.Emerald800
import com.sultanagung1.sista.core.designsystem.Slate100
import com.sultanagung1.sista.core.designsystem.Slate400
import com.sultanagung1.sista.core.designsystem.Slate500
import com.sultanagung1.sista.core.designsystem.Slate600
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.CalendarEvent
import com.sultanagung1.sista.data.model.EventType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AcademicCalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val tabs = listOf("Bulan", "Minggu", "Agenda")

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Kalender Akademik",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Kalender Akademik SMA Islam Sultan Agung 1")
                            putExtra(Intent.EXTRA_TEXT, "Sinkronisasi kalender akademik SMA Islam Sultan Agung 1 Semarang melalui iCal: https://sista.sultanagung1.sch.id/api/v1/academic-calendar/export-ical")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan iCal Agenda"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Sinkronisasi iCal", tint = Emerald700)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald700
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Text(
                                title,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // Filter Chip Row
            FilterChipRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategoryFilter(it) }
            )

            when (uiState.selectedTab) {
                0 -> MonthView(
                    uiState = uiState,
                    onPrevMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) },
                    onSelectDate = { viewModel.selectDate(it) },
                    onSelectEvent = onNavigateToEventDetail
                )
                1 -> WeekView(
                    uiState = uiState,
                    onSelectEvent = onNavigateToEventDetail
                )
                2 -> AgendaView(
                    uiState = uiState,
                    onSelectEvent = onNavigateToEventDetail
                )
            }
        }
    }
}

@Composable
fun FilterChipRow(
    selectedCategory: EventType?,
    onCategorySelected: (EventType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("Semua") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Emerald700,
                selectedLabelColor = Color.White
            )
        )
        EventType.entries.forEach { type ->
            FilterChip(
                selected = selectedCategory == type,
                onClick = { onCategorySelected(type) },
                label = { Text(type.displayName) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(type.getColor())
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = type.getColor(),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun MonthView(
    uiState: CalendarUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit,
    onSelectEvent: (String) -> Unit
) {
    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val currentMonthName = monthNames.getOrElse(uiState.month - 1) { "Bulan" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month navigation header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevMonth) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya")
                    }
                    Text(
                        "$currentMonthName ${uiState.year}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Emerald800
                    )
                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Selanjutnya")
                    }
                }
            }
        }

        // Calendar Grid
        item {
            CalendarGrid(
                year = uiState.year,
                month = uiState.month,
                selectedDate = uiState.selectedDate,
                events = uiState.filteredEvents,
                onSelectDate = onSelectDate
            )
        }

        // Selected Date Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Agenda: ${formatReadableDate(uiState.selectedDate)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${uiState.selectedDayEvents.size} Acara",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }
        }

        // Events for selected date
        if (uiState.selectedDayEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = Slate400, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada agenda sekolah di tanggal ini.", color = Slate600, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(uiState.selectedDayEvents) { event ->
                EventCard(event = event, onClick = { onSelectEvent(event.id) })
            }
        }
    }
}

@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    selectedDate: String,
    events: List<CalendarEvent>,
    onSelectDate: (String) -> Unit
) {
    val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Days header
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (day == "Min") AccentRose else Slate600
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Calendar weeks
            var currentDay = 1
            val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

            for (week in 0 until (totalCells / 7)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (dayOfWeek in 0..6) {
                        val cellIndex = week * 7 + dayOfWeek
                        if (cellIndex < firstDayOfWeek || currentDay > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val dayNum = currentDay
                            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNum)
                            val isSelected = dateStr == selectedDate
                            val dayEvents = events.filter { it.date == dateStr }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Emerald700
                                        else Color.Transparent
                                    )
                                    .clickable { onSelectDate(dateStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            dayOfWeek == 0 -> AccentRose
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (dayEvents.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            dayEvents.take(3).forEach { evt ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .padding(horizontal = 0.5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else evt.type.getColor())
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            currentDay++
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekView(
    uiState: CalendarUiState,
    onSelectEvent: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Jadwal Pekan Ini (Kurikulum Merdeka & Kegiatan)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        items(uiState.filteredEvents) { event ->
            EventCard(event = event, onClick = { onSelectEvent(event.id) })
        }
    }
}

@Composable
fun AgendaView(
    uiState: CalendarUiState,
    onSelectEvent: (String) -> Unit
) {
    val sortedEvents = remember(uiState.filteredEvents) {
        uiState.filteredEvents.sortedBy { it.date }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Daftar Lengkap Agenda Sekolah", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        items(sortedEvents) { event ->
            EventCard(event = event, onClick = { onSelectEvent(event.id) })
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Color indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(event.type.getColor())
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = event.type.getColor().copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = event.type.displayName,
                            color = event.type.getColor(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = event.date,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Slate400
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }

                if (event.startTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Slate400
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.startTime} - ${event.endTime ?: "Selesai"}",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }
            }

            IconButton(onClick = onClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Detail", tint = Slate400)
            }
        }
    }
}

fun formatReadableDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateStr)
        val outSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        if (date != null) outSdf.format(date) else dateStr
    } catch (_: Exception) {
        dateStr
    }
}
