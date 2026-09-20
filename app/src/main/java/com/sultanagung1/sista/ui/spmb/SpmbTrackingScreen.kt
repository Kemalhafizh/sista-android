package com.sultanagung1.sista.ui.spmb

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.TimelineEvent

@Composable
fun SpmbTrackingScreen(
    initialRegNumber: String? = null,
    viewModel: SpmbViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var queryInput by remember { mutableStateOf(initialRegNumber ?: uiState.trackingQuery) }

    LaunchedEffect(initialRegNumber) {
        if (!initialRegNumber.isNullOrBlank()) {
            queryInput = initialRegNumber
            viewModel.trackRegistration(initialRegNumber)
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pelacakan Status SPMB",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Search Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Masukkan Nomor Pendaftaran SPMB", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = queryInput,
                                onValueChange = { queryInput = it },
                                placeholder = { Text("Contoh: SPMB-2027-10492") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.trackRegistration(queryInput)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Cari")
                            }
                        }
                    }
                }
            }

            if (uiState.isTrackingLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Emerald700)
                    }
                }
            } else if (uiState.trackingInfo != null) {
                val info = uiState.trackingInfo!!

                // Status Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald800)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(info.registrationNumber, color = Gold300, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Emerald600
                                ) {
                                    Text(
                                        info.status,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(info.applicantName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(info.track, color = Slate200, fontSize = 13.sp)

                            if (info.cbtDate != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Gold400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Jadwal Tes CBT: ${info.cbtDate}", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Vertical Timeline
                item {
                    Text("Alur Proses Pendaftaran (Timeline)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(info.timeline) { event ->
                    TimelineItemRow(event = event)
                }

                // Download Card Action
                item {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Mengunduh Kartu Ujian Peserta SPMB (${info.registrationNumber})...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh Kartu Peserta Ujian Seleksi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Silakan masukkan nomor pendaftaran untuk melacak status berkas.", color = Slate600, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItemRow(event: TimelineEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (event.isCompleted) Emerald600 else Slate300),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (event.isCompleted) Icons.Default.Check else Icons.Default.Pending,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(36.dp)
                    .background(if (event.isCompleted) Emerald300 else Slate200)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                event.step,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (event.isCompleted) Emerald900 else Slate700
            )
            if (event.timestamp != null) {
                Text(event.timestamp, fontSize = 11.sp, color = Slate500)
            }
            if (event.description != null) {
                Text(event.description, fontSize = 12.sp, color = Slate600, lineHeight = 16.sp)
            }
        }
    }
}
