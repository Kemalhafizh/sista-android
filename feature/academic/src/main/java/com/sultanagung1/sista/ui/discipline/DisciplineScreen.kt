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
    val summary = uiState.summary
    val haptics = rememberHapticFeedbackHelper()

    var activeSignLetter by remember { mutableStateOf<WarningLetterItem?>(null) }
    val signatureStrokes = remember { androidx.compose.runtime.mutableStateListOf<SignatureStroke>() }
    var signatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }
    val snackbarHostState = remember { SnackbarHostState() }

    // The dialog stays open until the server confirms, so a failed save
    // doesn't throw away the parent's drawn signature.
    LaunchedEffect(uiState.signatureSuccess) {
        if (uiState.signatureSuccess) {
            haptics.success()
            activeSignLetter = null
            signatureStrokes.clear()
            viewModel.clearSignatureSuccess()
            snackbarHostState.showSnackbar("Tanda tangan tersimpan di server.")
        }
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Parent with several children: pick whose record to show. The
            // server used to always answer with the first child, whatever
            // child was selected on the dashboard.
            if (uiState.children.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text("Anak:", style = MaterialTheme.typography.labelMedium, color = Slate600)
                    }
                    items(uiState.children, key = { it.uuid }) { child ->
                        FilterChip(
                            selected = child.uuid == uiState.selectedChildUuid,
                            onClick = {
                                haptics.tapLight()
                                viewModel.selectChild(child.uuid)
                            },
                            label = {
                                Text(
                                    text = listOfNotNull(child.name, child.classroom).joinToString(" • "),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

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
                // Failures used to be stored and never shown, which is how an
                // endpoint that failed on every request went unnoticed.
                uiState.errorMessage?.let { message ->
                    item {
                        SulaoneErrorBanner(message = message, onRetry = { viewModel.loadData() })
                    }
                }
                if (uiState.isLoading) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Emerald600)
                    }
                }

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
                                                // Parents always see whose record this is
                                                // before reading — or signing — anything.
                                                if (uiState.children.isNotEmpty()) {
                                                    summary?.student?.let { student ->
                                                        Text(
                                                            text = listOfNotNull(student.name, student.classroom).joinToString(" • "),
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Emerald100
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Predikat: ${summary?.status ?: "–"}",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color.White.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = summary?.let { "Saldo ${it.totalPoints} poin" } ?: "Saldo –",
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
                                                        text = summary?.totalViolationPoints?.toString() ?: "–",
                                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                        // 30 = the server's "Perlu Perhatian" level, measured on the net balance.
                                                        color = if ((summary?.totalPoints ?: 0) >= 30) AccentRose else Gold400
                                                    )
                                                    Text(
                                                        text = when {
                                                            summary == null -> "–"
                                                            summary.nextWarningLevel != null && summary.nextWarningThreshold != null ->
                                                                "${summary.nextWarningLevel} saat saldo ${summary.nextWarningThreshold} poin"
                                                            else -> "Batas SP3 sudah tercapai"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = Emerald200
                                                    )
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
                                                        text = summary?.let { "+${it.totalRewardPoints}" } ?: "–",
                                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = Emerald300
                                                    )
                                                    Text("Mengurangi saldo poin", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Emerald200)
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
                                // The school's active point categories from the server
                                // (this used to be a hardcoded list of four rules).
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val rules = summary?.rules.orEmpty()
                                    when {
                                        summary == null -> Text(
                                            text = "Aturan poin tampil setelah data dimuat.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                        rules.isEmpty() -> Text(
                                            text = "Sekolah belum mengatur kategori poin tata tertib.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                        else -> rules.forEachIndexed { index, rule ->
                                            if (index > 0) {
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                                            }
                                            RuleItem(
                                                title = rule.name,
                                                points = if (rule.type == "reward") "−${rule.points} poin (kebaikan)" else "+${rule.points} poin"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Riwayat Kasus Pelanggaran & Reward
                        if (uiState.recordsLoaded && uiState.records.isEmpty()) {
                            item {
                                SulaoneEmptyState(
                                    title = "Belum Ada Catatan Poin",
                                    description = "Belum ada pelanggaran maupun poin kebaikan yang dicatat.",
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        }
                        items(uiState.records, key = { it.id }) { record ->
                            val isReward = record.type == "reward"
                            val badge = when (record.type) {
                                "reward" -> "−${kotlin.math.abs(record.points)} POIN KEBAIKAN"
                                "penalty" -> "+${kotlin.math.abs(record.points)} POIN"
                                else -> "PENYESUAIAN ${if (record.points > 0) "+" else ""}${record.points}"
                            }
                            val title = record.categoryName ?: when (record.type) {
                                "reward" -> "Poin Kebaikan"
                                "penalty" -> "Pelanggaran"
                                else -> "Penyesuaian Poin"
                            }
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
                                                text = badge,
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
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isReward) Emerald800 else AccentRose
                                    )
                                    if (!record.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = record.description.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
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
                        // "No letters" is only claimed once the server said so —
                        // not while loading or after a failed request.
                        if (uiState.warningLetters.isEmpty()) {
                            if (uiState.lettersLoaded) item {
                                SulaoneEmptyState(
                                    title = "Alhamdulillah, Tidak Ada Surat Peringatan",
                                    description = "Pertahankan perilaku terpuji dan kedisiplinan Anda di SMA Islam Sultan Agung 1.",
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        } else {
                            items(uiState.warningLetters, key = { it.id }) { letter ->
                                ModernBentoCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    borderColor = if (letter.isSigned) Emerald300 else AccentRose.copy(alpha = 0.4f),
                                    glowColor = if (letter.isSigned) null else RoseGlow
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (letter.isSigned) Emerald700 else AccentRose
                                            ) {
                                                Text(
                                                    text = letter.level.orEmpty(),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            LiveStatusChip(
                                                text = if (letter.isSigned) "Tertandatangani Ortu" else "Menunggu TTD Ortu",
                                                color = if (letter.isSigned) Emerald700 else AccentRose
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = listOfNotNull(
                                                letter.issuedAt?.let { "Terbit $it" },
                                                "saldo ≥ ${letter.pointThreshold} poin"
                                            ).joinToString(" • "),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Slate600
                                        )
                                        if (!letter.notes.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = letter.notes.orEmpty(),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        when {
                                            letter.isSigned -> Text(
                                                text = "✓ Ditandatangani orang tua/wali pada ${letter.signedAt.orEmpty()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Emerald700
                                            )
                                            // Only the student's own parent gets the pad; the
                                            // server rejects anyone else's signature anyway.
                                            letter.canSign -> SulaoneButton(
                                                text = "Buka Lembar Tanda Tangan Digital",
                                                onClick = {
                                                    haptics.tapHeavy()
                                                    viewModel.clearSignError()
                                                    activeSignLetter = letter
                                                },
                                                icon = Icons.Default.Draw
                                            )
                                            else -> Text(
                                                text = "Menunggu tanda tangan orang tua/wali melalui akun orang tua.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate600
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
            if (!uiState.isSigning) {
                activeSignLetter = null
                signatureStrokes.clear()
            }
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
                        text = listOfNotNull(
                            "Pengesahan Surat Peringatan ${letter.level.orEmpty()}",
                            summary?.student?.name?.let { "untuk $it" }
                        ).joinToString(" "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    // Name/phone fields were removed: the server never read them —
                    // the signer is the logged-in parent account.
                    Text(
                        text = "Ditandatangani atas nama akun orang tua yang sedang masuk.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
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

                    uiState.signError?.let { message ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum tersimpan: $message",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentRose
                        )
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
                            enabled = !uiState.isSigning,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                val base64Signature = captureSignatureAsBase64Png(signatureStrokes, signatureCanvasSize)
                                    ?: return@Button
                                haptics.tapHeavy()
                                // The dialog closes on the server's confirmation
                                // (see the signatureSuccess effect), not here.
                                viewModel.signWarningLetter(letter.id, base64Signature)
                            },
                            enabled = signatureStrokes.isNotEmpty() && !uiState.isSigning,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            if (uiState.isSigning) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text("Sahkan TTD Digital", fontWeight = FontWeight.Bold)
                            }
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
