package com.sultanagung1.sista.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

data class ProctorStudentItem(
    val id: Long,
    val name: String,
    val nisn: String,
    val classroom: String,
    val answeredCount: Int,
    val totalQuestions: Int,
    val status: String, // "WORKING", "WARNING_1", "WARNING_2", "LOCKED", "SUBMITTED"
    val violationReason: String? = null,
    val lastPingSecondsAgo: Int = 2
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProctorDashboardScreen(
    examId: Long = 101,
    examTitle: String = "Penilaian Tengah Semester (PTS) Ganjil - Fisika Fase F",
    onNavigateBack: () -> Unit
) {
    var studentList by remember {
        mutableStateOf(
            listOf(
                ProctorStudentItem(1, "Ahmad Kemal Hafizh", "0069911223", "XII MIPA 1", 24, 25, "WORKING"),
                ProctorStudentItem(2, "Budi Wicaksono", "0069911224", "XII MIPA 1", 18, 25, "LOCKED", "Beralih aplikasi 3x (Membuka browser)"),
                ProctorStudentItem(3, "Citra Dewi Maharani", "0069911225", "XII MIPA 1", 20, 25, "WARNING_2", "Mencoba screenshot layar ujian"),
                ProctorStudentItem(4, "Dimas Pratama", "0069911226", "XII MIPA 1", 25, 25, "SUBMITTED"),
                ProctorStudentItem(5, "Eka Nur Cahyani", "0069911227", "XII MIPA 1", 16, 25, "WARNING_1", "Panel notifikasi ditarik ke bawah"),
                ProctorStudentItem(6, "Farhan Maulana", "0069911228", "XII MIPA 1", 22, 25, "WORKING")
            )
        )
    }

    var selectedStudentForAction by remember { mutableStateOf<ProctorStudentItem?>(null) }
    var generatedUnlockPin by remember { mutableStateOf<String?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastMessage by remember { mutableStateOf("") }
    var broadcastSentSuccess by remember { mutableStateOf(false) }

    // 5-Minute Token Lifespan State
    var activeToken by remember { mutableStateOf("K7X4M2") }
    var tokenRemainingSeconds by remember { mutableIntStateOf(300) }
    val haptics = com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper()

    LaunchedEffect(activeToken) {
        tokenRemainingSeconds = 300
        while (tokenRemainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            tokenRemainingSeconds--
        }
    }

    val lockedCount = studentList.count { it.status == "LOCKED" }
    val workingCount = studentList.count { it.status.startsWith("W") }
    val submittedCount = studentList.count { it.status == "SUBMITTED" }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pengawas Ujian Daring (Proctor)",
                subtitle = examTitle,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Summary Bento Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Working Students
                    ModernBentoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = Emerald50,
                        borderColor = Emerald200,
                        glowColor = EmeraldGlow
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SEDANG UJIAN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$workingCount Siswa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald900
                            )
                        }
                    }

                    // Locked Students (Anti-Cheat triggered)
                    ModernBentoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (lockedCount > 0) AccentRose.copy(alpha = 0.1f) else Slate50,
                        borderColor = if (lockedCount > 0) AccentRose.copy(alpha = 0.3f) else Slate200,
                        glowColor = if (lockedCount > 0) RoseGlow else Color.Transparent
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TERKUNCI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (lockedCount > 0) AccentRose else Slate600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$lockedCount Siswa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lockedCount > 0) AccentRose else Slate800
                            )
                        }
                    }

                    // Finished Students
                    ModernBentoCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentBlue.copy(alpha = 0.1f),
                        borderColor = AccentBlue.copy(alpha = 0.3f),
                        glowColor = SapphireGlow
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "KUMPUL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$submittedCount Siswa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentBlue
                            )
                        }
                    }
                }
            }

            // FASE 87: CBT Active Exam Token & 5-Minute Countdown Timer
            item {
                val isExpired = tokenRemainingSeconds <= 0
                val timerMinutes = tokenRemainingSeconds / 60
                val timerSeconds = tokenRemainingSeconds % 60
                val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", timerMinutes, timerSeconds)

                val tokenAccentColor = when {
                    isExpired -> AccentRose
                    tokenRemainingSeconds < 60 -> AccentRose
                    tokenRemainingSeconds < 120 -> Gold600
                    else -> Emerald700
                }

                val tokenBgColor = when {
                    isExpired -> AccentRose.copy(alpha = 0.1f)
                    tokenRemainingSeconds < 60 -> AccentRose.copy(alpha = 0.1f)
                    tokenRemainingSeconds < 120 -> Gold50
                    else -> Emerald50
                }

                val tokenBorderColor = when {
                    isExpired -> AccentRose.copy(alpha = 0.35f)
                    tokenRemainingSeconds < 60 -> AccentRose.copy(alpha = 0.35f)
                    tokenRemainingSeconds < 120 -> Gold300
                    else -> Emerald200
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tokenBgColor,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, tokenBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tokenAccentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = tokenAccentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "TOKEN MASUK UJIAN (CBT)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = tokenAccentColor
                                    )
                                    Text(
                                        text = "Masa Aktif: Tepat 5 Menit (Standar ANBK)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate600
                                    )
                                }
                            }

                            // Expiry Status Badge with live countdown
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isExpired) AccentRose.copy(alpha = 0.15f) else tokenAccentColor.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isExpired) Icons.Default.Warning else Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = tokenAccentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isExpired) "KEDALUWARSA" else timeFormatted,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        ),
                                        color = tokenAccentColor
                                    )
                                }
                            }
                        }

                        // Giant Monospace Token Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, tokenBorderColor, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isExpired) "------" else activeToken.chunked(1).joinToString(" "),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 6.sp
                                ),
                                color = if (isExpired) Slate400 else Slate900
                            )
                        }

                        // Live Progress Bar (300s)
                        LinearProgressIndicator(
                            progress = { tokenRemainingSeconds / 300f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = tokenAccentColor,
                            trackColor = Color.White.copy(alpha = 0.8f)
                        )

                        // Action Buttons: Regenerate Token & Alert
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptics.tapHeavy()
                                    val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
                                    activeToken = (1..6).map { chars.random() }.joinToString("")
                                    tokenRemainingSeconds = 300
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tokenAccentColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isExpired) "Rilis Token Baru (5 Mnt)" else "Refresh Token (5 Mnt)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showBroadcastDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Pengumuman", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            // Generate instant universal emergency unlock code
                            generatedUnlockPin = "SA1-${(1000..9999).random()}"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Token Darurat", fontSize = 12.sp)
                    }
                }
            }

            // Display emergency token banner if generated
            if (generatedUnlockPin != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Gold100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold400),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TOKEN BUKA KUNCI PENGAWAS:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold800
                                )
                                Text(
                                    text = generatedUnlockPin ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "Berikan kode ini kepada siswa yang terkunci untuk melanjutkan.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Slate700
                                )
                            }
                            IconButton(onClick = { generatedUnlockPin = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Gold800)
                            }
                        }
                    }
                }
            }

            // Student Proctor List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Peserta Ujian Live (${studentList.size} Siswa)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Sync", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                    }
                }
            }

            // Student Item Cards
            items(studentList) { student ->
                SulaoneCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedStudentForAction = student },
                    elevation = 2.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "NISN: ${student.nisn} • ${student.classroom}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Slate600
                                )
                            }

                            // Status Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (student.status) {
                                    "LOCKED" -> AccentRose.copy(alpha = 0.15f)
                                    "WARNING_2" -> AccentAmber.copy(alpha = 0.15f)
                                    "WARNING_1" -> Gold400.copy(alpha = 0.2f)
                                    "SUBMITTED" -> AccentBlue.copy(alpha = 0.15f)
                                    else -> AccentGreen.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = when (student.status) {
                                        "LOCKED" -> "TERKUNCI"
                                        "WARNING_2" -> "PERINGATAN 2"
                                        "WARNING_1" -> "PERINGATAN 1"
                                        "SUBMITTED" -> "SELESAI"
                                        else -> "AKTIF"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (student.status) {
                                        "LOCKED" -> AccentRose
                                        "WARNING_2" -> AccentAmber
                                        "WARNING_1" -> Gold800
                                        "SUBMITTED" -> AccentBlue
                                        else -> AccentGreen
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { student.answeredCount.toFloat() / student.totalQuestions },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (student.status == "LOCKED") AccentRose else Emerald700,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${student.answeredCount}/${student.totalQuestions} Soal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        }

                        if (student.violationReason != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ ${student.violationReason}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AccentRose
                            )
                        }
                    }
                }
            }
        }
    }

    // Action dialog for selected student
    if (selectedStudentForAction != null) {
        val student = selectedStudentForAction!!
        AlertDialog(
            onDismissRequest = { selectedStudentForAction = null },
            icon = {
                Icon(
                    imageVector = if (student.status == "LOCKED") Icons.Default.Lock else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (student.status == "LOCKED") AccentRose else Emerald700
                )
            },
            title = { Text(student.name) },
            text = {
                Column {
                    Text("NISN: ${student.nisn} (${student.classroom})")
                    Text("Status: ${student.status}")
                    Text("Progres: ${student.answeredCount} dari ${student.totalQuestions} soal dijawab")
                    if (student.violationReason != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Riwayat: ${student.violationReason}",
                            color = AccentRose,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (student.status == "LOCKED") {
                    Button(
                        onClick = {
                            // Unlock student
                            studentList = studentList.map {
                                if (it.id == student.id) it.copy(status = "WORKING", violationReason = null)
                                else it
                            }
                            selectedStudentForAction = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text("Buka Kunci Sekarang")
                    }
                } else {
                    Button(
                        onClick = { selectedStudentForAction = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Tutup")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStudentForAction = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Broadcast message dialog
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Siarkan Pesan ke Seluruh Peserta") },
            text = {
                Column {
                    Text(
                        "Pesan ini akan langsung muncul sebagai notifikasi pop-up di layar seluruh siswa yang sedang mengerjakan ujian.",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = broadcastMessage,
                        onValueChange = { broadcastMessage = it },
                        placeholder = { Text("Contoh: Waktu pengerjaan tersisa 15 menit lagi.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBroadcastDialog = false
                        broadcastMessage = ""
                        broadcastSentSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Kirim Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
