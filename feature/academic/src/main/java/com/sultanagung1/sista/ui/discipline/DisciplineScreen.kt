package com.sultanagung1.sista.ui.discipline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.WarningLetterItem
import com.sultanagung1.sista.ui.common.SignaturePad
import com.sultanagung1.sista.ui.common.SignatureStroke
import com.sultanagung1.sista.ui.common.captureSignatureAsBase64Png

@Composable
fun DisciplineScreen(
    viewModel: DisciplineViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()

    var activeSignLetter by remember { mutableStateOf<WarningLetterItem?>(null) }
    var parentNameInput by remember { mutableStateOf("") }
    var parentPhoneInput by remember { mutableStateOf("") }
    val signatureStrokes = remember { androidx.compose.runtime.mutableStateListOf<SignatureStroke>() }
    var signatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }

    val tabs = listOf("Buku Saku Poin", "Riwayat Kasus", "Surat Peringatan (SP)")

    val pointGradient = remember {
        Brush.linearGradient(
            colors = listOf(Emerald800, Emerald600, Emerald400)
        )
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Tata Tertib & Poin Siswa",
                subtitle = "Buku Saku Digital SMA Islam Sultan Agung 1",
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
            // Tabs Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.indices.toList()) { index ->
                    val isSelected = uiState.selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                if (isSelected) Emerald600 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            )
                            .springPressable {
                                haptics.tapLight()
                                viewModel.selectTab(index)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabs[index],
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (uiState.selectedTab) {
                    0 -> {
                        // 1. Point Gauge & Summary Banner
                        item {
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = Color.Transparent,
                                glowColor = EmeraldGlow
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(pointGradient)
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "STATUS KEDISIPLINAN",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Gold400
                                                )
                                                Text(
                                                    text = "Predikat: ${uiState.summary.pointStatus ?: "BAIK"}",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color.White.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Maks 100 Poin",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Comparison Matrix
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Poin Pelanggaran
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.Black.copy(alpha = 0.2f),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text("Poin Pelanggaran", style = MaterialTheme.typography.labelSmall, color = Emerald100)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "${uiState.summary.totalViolationPoints}",
                                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = if (uiState.summary.totalViolationPoints > 30) AccentRose else Gold400
                                                    )
                                                    Text("Batas SP1: 25 Poin", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Emerald200)
                                                }
                                            }

                                            // Poin Prestasi / Reward
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.Black.copy(alpha = 0.2f),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text("Poin Kebaikan", style = MaterialTheme.typography.labelSmall, color = Emerald100)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "+${uiState.summary.totalRewardPoints}",
                                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = Emerald300
                                                    )
                                                    Text("Pengurang Poin SP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Emerald200)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Panduan & Aturan Tata Tertib SMA",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        item {
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    RuleItem("Keterlambatan Hadir (> 07:00)", "5 Poin")
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                                    RuleItem("Atribut Seragam / Peci Tidak Lengkap", "10 Poin")
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                                    RuleItem("Meninggalkan KBM Tanpa Izin Guru Piket", "15 Poin")
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                                    RuleItem("Meraih Prestasi / Juara Lomba Mewakili Sekolah", "-25 Poin (Bonus)")
                                }
                            }
                        }
                    }

                    1 -> {
                        // Riwayat Kasus Pelanggaran & Reward
                        items(uiState.records) { record ->
                            val isReward = record.category == "REWARD"
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = if (isReward) Emerald50 else AccentRose.copy(alpha = 0.08f),
                                borderColor = if (isReward) Emerald300 else AccentRose.copy(alpha = 0.3f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isReward) Emerald700 else AccentRose
                                        ) {
                                            Text(
                                                text = if (isReward) "+${record.points} REWARD" else "-${record.points} POIN",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = record.date.orEmpty(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = record.title.orEmpty(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isReward) Emerald800 else AccentRose
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = record.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700
                                    )

                                    if (!record.actionTaken.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.8f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                                        ) {
                                            Text(
                                                text = "Tindakan: ${record.actionTaken}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = Slate800,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Dicatat oleh: ${record.recordedBy.orEmpty()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Surat Peringatan (SP) Tab
                        if (uiState.warningLetters.isEmpty()) {
                            item {
                                SulaoneEmptyState(
                                    title = "Alhamdulillah, Tidak Ada Surat Peringatan",
                                    description = "Pertahankan perilaku terpuji dan kedisiplinan Anda di SMA Islam Sultan Agung 1.",
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        } else {
                            items(uiState.warningLetters) { letter ->
                                ModernBentoCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    borderColor = if (letter.isSignedByParent) Emerald300 else AccentRose.copy(alpha = 0.4f),
                                    glowColor = if (letter.isSignedByParent) null else RoseGlow
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (letter.isSignedByParent) Emerald700 else AccentRose
                                            ) {
                                                Text(
                                                    text = letter.level.orEmpty(),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            LiveStatusChip(
                                                text = if (letter.isSignedByParent) "Tertandatangani Ortu" else "Menunggu TTD Ortu",
                                                color = if (letter.isSignedByParent) Emerald700 else AccentRose
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "No: ${letter.letterNumber.orEmpty()}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Slate600
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = letter.reason.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (!letter.isSignedByParent) {
                                            SulaoneButton(
                                                text = "Buka Lembar Tanda Tangan Digital",
                                                onClick = {
                                                    haptics.tapHeavy()
                                                    activeSignLetter = letter
                                                },
                                                icon = Icons.Default.Draw
                                            )
                                        } else {
                                            Text(
                                                text = "✓ Disetujui oleh Wali Murid pada ${letter.parentSignedAt.orEmpty()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Emerald700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    // Modal Digital Signature SP Ortu
    if (activeSignLetter != null) {
        val letter = activeSignLetter!!
        Dialog(onDismissRequest = {
            activeSignLetter = null
            signatureStrokes.clear()
        }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Tanda Tangan Digital Wali Murid",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pengesahan Surat Peringatan ${letter.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = parentNameInput,
                        onValueChange = { parentNameInput = it },
                        label = { Text("Nama Lengkap Orang Tua / Wali") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = parentPhoneInput,
                        onValueChange = { parentPhoneInput = it },
                        label = { Text("Nomor WhatsApp Ortu") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real touch-drawable signature pad — this used to be a
                    // decorative, non-interactive placeholder box, and the
                    // "signature" actually submitted was a hardcoded mock
                    // string regardless of what (if anything) was drawn here.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, if (signatureStrokes.isEmpty()) Slate300 else Emerald400, RoundedCornerShape(14.dp))
                    ) {
                        SignaturePad(
                            strokes = signatureStrokes,
                            onCanvasSized = { signatureCanvasSize = it },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (signatureStrokes.isEmpty()) {
                            Text(
                                text = "Sentuh & goreskan tanda tangan di sini...",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    if (signatureStrokes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { signatureStrokes.removeLastOrNull() }) {
                                Text("Undo", fontSize = 12.sp)
                            }
                            TextButton(onClick = { signatureStrokes.clear() }) {
                                Text("Bersihkan", color = AccentRose, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                activeSignLetter = null
                                signatureStrokes.clear()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        val canSubmit = parentNameInput.isNotBlank() && parentPhoneInput.isNotBlank() && signatureStrokes.isNotEmpty()
                        Button(
                            onClick = {
                                val base64Signature = captureSignatureAsBase64Png(signatureStrokes, signatureCanvasSize)
                                if (base64Signature == null) return@Button
                                haptics.success()
                                viewModel.signWarningLetter(
                                    letter.id,
                                    base64Signature,
                                    parentNameInput,
                                    parentPhoneInput
                                )
                                activeSignLetter = null
                                signatureStrokes.clear()
                            },
                            enabled = canSubmit,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Sahkan TTD Digital", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleItem(title: String, points: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Slate700, modifier = Modifier.weight(1f))
        Text(text = points, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Emerald800)
    }
}
