package com.sultanagung1.sista.ui.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.security.BiometricAvailability
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.data.model.OsisCandidateItem
import com.sultanagung1.sista.data.model.TeacherEvaluationItem

@Composable
fun TeacherEvaluationScreen(
    viewModel: EvaluationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()
    val context = LocalContext.current

    var activeTeacherEval by remember { mutableStateOf<TeacherEvaluationItem?>(null) }
    var activeVoteCandidate by remember { mutableStateOf<OsisCandidateItem?>(null) }

    var pedagogyRating by remember { mutableStateOf(5) }
    var punctualityRating by remember { mutableStateOf(5) }
    var mannerRating by remember { mutableStateOf(5) }
    var feedbackInput by remember { mutableStateOf("") }

    val tabs = listOf("Evaluasi Guru (EKG)", "Survey Fasilitas", "E-Voting OSIS")

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.evaluationSuccess) {
        if (uiState.evaluationSuccess) {
            snackbarHostState.showSnackbar("Evaluasi guru berhasil dikirim secara anonim.")
            viewModel.clearSuccessFlags()
        }
    }
    LaunchedEffect(uiState.facilitySurveySuccess) {
        if (uiState.facilitySurveySuccess) {
            snackbarHostState.showSnackbar("Survei fasilitas berhasil dikirim.")
            viewModel.clearSuccessFlags()
        }
    }
    LaunchedEffect(uiState.voteSuccess) {
        if (uiState.voteSuccess) {
            snackbarHostState.showSnackbar("Suara Anda berhasil dicatat. Terima kasih!")
            viewModel.clearSuccessFlags()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SulaoneTopBar(
                title = "Kuesioner, Evaluasi & E-Voting",
                subtitle = "Umpan Balik Kinerja & Demokrasi Digital Siswa",
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
            // Tab Selector
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
                        // Banner Prasyarat Buka Rapor
                        item {
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = Emerald50,
                                borderColor = Emerald300,
                                glowColor = EmeraldGlow
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Emerald800, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Evaluasi Kinerja Guru Bersifat 100% Anonim",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Emerald900
                                        )
                                        Text(
                                            text = "Penilaian jujur Anda merupakan syarat pembukaan nilai Rapor Semester Kurikulum Merdeka.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Emerald700
                                        )
                                    }
                                }
                            }
                        }

                        items(uiState.teacherEvaluations) { eval ->
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = if (eval.isSubmitted) Emerald300 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = eval.subjectName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Emerald700
                                        )
                                        LiveStatusChip(
                                            text = if (eval.isSubmitted) "✓ Sudah Dinilai" else "Belum Dinilai",
                                            color = if (eval.isSubmitted) Emerald700 else AccentAmber
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = eval.teacherName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!eval.className.isNullOrBlank()) {
                                        Text(
                                            text = "Pengampu Kelas: ${eval.className}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (!eval.isSubmitted) {
                                        SulaoneButton(
                                            text = "Isi Kuesioner Evaluasi",
                                            onClick = {
                                                haptics.tapHeavy()
                                                activeTeacherEval = eval
                                            },
                                            icon = Icons.Default.RateReview
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Slate100
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Gold400, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Rating Anda: Pedagogik (${eval.pedagogyRating}★), Ketepatan (${eval.punctualityRating}★), Karakter (${eval.islamicMannerRating}★)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    color = Slate700
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Survey Fasilitas Tab
                        item {
                            Text(
                                text = "Polling Kualitas Fasilitas & Sarpras Yayasan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(uiState.facilitySurveys) { survey ->
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = if (survey.isSubmitted) Emerald300 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = survey.facilityName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (survey.isSubmitted) {
                                            LiveStatusChip(text = "✓ Terkirim", color = Emerald700)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Tingkat Kepuasan:", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                        Row {
                                            for (star in 1..5) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Beri rating $star bintang",
                                                    tint = if (star <= survey.satisfactionLevel) Gold400 else Slate300,
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .springPressable {
                                                            if (!survey.isSubmitted) {
                                                                haptics.tapLight()
                                                                viewModel.rateFacility(survey.id, star)
                                                            }
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SulaoneButton(
                                text = "Kirim Survei Fasilitas",
                                onClick = {
                                    haptics.success()
                                    viewModel.submitFacilitySurveys()
                                },
                                icon = Icons.Default.Send,
                                enabled = uiState.facilitySurveys.any { it.satisfactionLevel > 0 && !it.isSubmitted }
                            )
                        }
                    }

                    2 -> {
                        // E-Voting Ketua OSIS Tab
                        item {
                            Text(
                                text = "Pemilihan Langsung Ketua OSIS Periode 2026/2027",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(uiState.osisCandidates) { candidate ->
                            val isVoted = candidate.isVoted
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = if (isVoted) Emerald50 else MaterialTheme.colorScheme.surface,
                                borderColor = if (isVoted) Emerald400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                glowColor = if (isVoted) EmeraldGlow else null
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Emerald700
                                        ) {
                                            Text(
                                                text = "PASLON 0${candidate.number}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                        LiveStatusChip(
                                            text = if (isVoted) "✓ Pilihan Anda (${candidate.totalVotes} Suara)" else "${candidate.totalVotes} Suara Masuk",
                                            color = if (isVoted) Emerald700 else Slate700
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = candidatePairLabel(candidate),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Visi: ${candidate.vision}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = Slate700
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (!isVoted) {
                                        SulaoneButton(
                                            text = "Gunakan Hak Suara (Coblos Paslon 0${candidate.number})",
                                            onClick = {
                                                haptics.tapHeavy()
                                                activeVoteCandidate = candidate
                                            },
                                            icon = Icons.Default.HowToVote
                                        )
                                    } else {
                                        Text(
                                            text = "🔒 Hak suara Anda telah terverifikasi secara aman dan rahasia.",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Emerald700
                                        )
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

    // Modal EKG Kuesioner Guru
    if (activeTeacherEval != null) {
        val eval = activeTeacherEval!!
        Dialog(onDismissRequest = { activeTeacherEval = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Evaluasi Kinerja Guru",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${eval.teacherName} • ${eval.subjectName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RatingRow("Kemampuan Mengajar (Pedagogik)", pedagogyRating) { pedagogyRating = it }
                    RatingRow("Ketepatan Waktu KBM", punctualityRating) { punctualityRating = it }
                    RatingRow("Karakter & Nilai Islami", mannerRating) { mannerRating = it }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        label = { Text("Saran & Masukan Konstruktif (Opsional)") },
                        placeholder = { Text("Komentar anonim untuk perbaikan...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeTeacherEval = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                haptics.success()
                                viewModel.submitTeacherEvaluation(eval.id, pedagogyRating, punctualityRating, mannerRating, feedbackInput)
                                activeTeacherEval = null
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Kirim Anonim", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal E-Voting Confirmation
    if (activeVoteCandidate != null) {
        val candidate = activeVoteCandidate!!
        Dialog(onDismissRequest = { activeVoteCandidate = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Konfirmasi Hak Suara E-Voting",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Anda akan memilih Paslon 0${candidate.number} (${candidatePairLabel(candidate)}) sebagai Ketua & Wakil Ketua OSIS. Pilihan tidak dapat diubah setelah dikonfirmasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeVoteCandidate = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                // The backend does not cryptographically verify
                                // biometric_signature (it's an unread, always-
                                // required string — see castVote()'s "Simulated
                                // biometric validation" comment), so the real
                                // security value here is this LOCAL fingerprint
                                // gate itself: it confirms the device owner is
                                // the one casting the vote, not a stranger who
                                // picked up an unlocked phone.
                                val activity = context as? FragmentActivity
                                if (BiometricVault.checkStatus(context) == BiometricAvailability.AVAILABLE && activity != null) {
                                    BiometricVault.authenticate(
                                        activity = activity,
                                        title = "Verifikasi Sidik Jari Pemilih",
                                        subtitle = "Konfirmasi identitas Anda sebelum suara dikirim",
                                        negativeButtonText = "Batal",
                                        onSuccess = {
                                            haptics.success()
                                            viewModel.castOsisVote(candidate.id, "biometric_verified_${System.currentTimeMillis()}")
                                            activeVoteCandidate = null
                                        }
                                    )
                                } else {
                                    haptics.success()
                                    viewModel.castOsisVote(candidate.id, "biometric_unavailable_on_device")
                                    activeVoteCandidate = null
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Coblos Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun candidatePairLabel(candidate: OsisCandidateItem): String {
    val president = candidate.presidentName ?: "Kandidat Ketua"
    return candidate.vicePresidentName?.let { "$president & $it" } ?: president
}

@Composable
private fun RatingRow(title: String, currentRating: Int, onRatingSelected: (Int) -> Unit) {
    val haptics = rememberHapticFeedbackHelper()

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = Slate700)
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            for (star in 1..5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (star <= currentRating) Gold400 else Slate300,
                    modifier = Modifier
                        .size(28.dp)
                        .springPressable {
                            haptics.tapLight()
                            onRatingSelected(star)
                        }
                )
            }
        }
    }
}
