package com.sultanagung1.sista.ui.cbt

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.security.CbtAntiCheatEngine
import com.sultanagung1.sista.core.security.ExamViolationType
import com.sultanagung1.sista.ui.cbt.components.CbtImageViewer
import com.sultanagung1.sista.ui.cbt.components.CbtLatexMathView
import java.util.Locale

// Matches the previous hardcoded default; no per-exam duration field exists in CbtExamQuestion yet.
private const val EXAM_DURATION_SECONDS = 5400L

@Composable
fun CbtExamRoomScreen(
    examId: Long,
    studentId: Long?,
    maxViolations: Int?,
    viewModel: CbtViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val antiCheatEngine = remember(maxViolations) {
        if (maxViolations != null) CbtAntiCheatEngine(context, maxViolations) else CbtAntiCheatEngine(context)
    }
    val antiCheatState by antiCheatEngine.antiCheatState.collectAsState()

    val uiState by viewModel.uiState.collectAsState()
    // FASE 69.1: the countdown now lives in the ViewModel (SavedStateHandle-backed,
    // wall-clock based) so it survives process death instead of resetting to 5400s.
    val remainingSeconds = uiState.remainingSeconds
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitWarningDialog by remember { mutableStateOf(false) }

    val lockTaskManager = remember { com.sultanagung1.sista.core.security.CbtLockTaskManager(context) }
    val livenessProctor = remember { com.sultanagung1.sista.core.security.RandomLivenessProctor(context) }
    val livenessState by livenessProctor.livenessState.collectAsState()
    val vault = remember { com.sultanagung1.sista.core.security.CbtEncryptedVault(context) }

    var supervisorPinInput by remember { mutableStateOf("") }
    var supervisorPinError by remember { mutableStateOf<String?>(null) }

    // Check Vault or load questions
    LaunchedEffect(examId) {
        if (vault.hasEncryptedPayload(examId)) {
            viewModel.unlockFromVaultWithKey(examId, vault)
        } else {
            viewModel.loadQuestions(examId)
        }
    }

    val hasExitedRef = remember { mutableStateOf(false) }

    // Release kiosk mode if submitted or force-closed
    LaunchedEffect(uiState.isSubmitted, uiState.isForceClosedBySystem) {
        if ((uiState.isSubmitted || uiState.isForceClosedBySystem) && activity != null) {
            antiCheatEngine.deactivateExamSecurity(activity)
            lockTaskManager.stopKioskMode(activity)
            livenessProctor.stopMonitoring()
            vault.clearVault(examId)
        }
    }

    // Activate hardware-level window protection (FLAG_SECURE), Kiosk Lock Task, and anti-cheat listeners
    DisposableEffect(lifecycleOwner) {
        if (activity != null) {
            antiCheatEngine.activateExamSecurity(activity)
            lockTaskManager.startKioskMode(activity)
            livenessProctor.startMonitoring()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // FASE 26: Auto-close on exit / minimize / split screen
                    if (!hasExitedRef.value && !uiState.isSubmitted) {
                        hasExitedRef.value = true
                        val reason = if (activity?.isInMultiWindowMode == true) "split_screen" else "app_minimized"
                        viewModel.forceCloseExam(examId, reason, vault)
                        if (activity != null) {
                            antiCheatEngine.deactivateExamSecurity(activity)
                            lockTaskManager.stopKioskMode(activity)
                            livenessProctor.stopMonitoring()
                        }
                        onNavigateBack()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (!hasExitedRef.value && !uiState.isSubmitted) {
                        hasExitedRef.value = true
                        viewModel.forceCloseExam(examId, "app_closed", vault)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (activity != null) {
                        antiCheatEngine.verifyMultiWindowMode(activity)
                    }
                    if (uiState.isForceClosedBySystem) {
                        onNavigateBack()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (activity != null) {
                antiCheatEngine.deactivateExamSecurity(activity)
                lockTaskManager.stopKioskMode(activity)
                livenessProctor.stopMonitoring()
            }
        }
    }

    // FASE 26 rule kept: leaving an active exam closes it. What changed (76.3):
    // leaving is an explicit, confirmed action, never a side effect of a single
    // system back gesture. An edge swipe on gesture navigation is easy to make by
    // accident, and it used to force-close and submit the exam instantly.
    val exitAndForceClose: (String) -> Unit = { reason ->
        if (!hasExitedRef.value && !uiState.isSubmitted) {
            hasExitedRef.value = true
            viewModel.forceCloseExam(examId, reason, vault)
            if (activity != null) {
                antiCheatEngine.deactivateExamSecurity(activity)
                lockTaskManager.stopKioskMode(activity)
                livenessProctor.stopMonitoring()
            }
            onNavigateBack()
        }
    }

    // Back (button or swipe) is consumed for the whole exam session, so there is
    // no predictive-back peek of the previous screen either. While the exam is
    // running it only asks; after submit/force-close it leaves normally.
    BackHandler(enabled = true) {
        if (!hasExitedRef.value && !uiState.isSubmitted && !uiState.isForceClosedBySystem) {
            showExitWarningDialog = true
        } else {
            onNavigateBack()
        }
    }

    // FASE 69.1: the ViewModel owns the countdown (wall-clock deadline persisted in
    // SavedStateHandle), so this effect only starts it once instead of ticking locally.
    LaunchedEffect(examId) {
        viewModel.startExamTimer(EXAM_DURATION_SECONDS)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.pauseExamTimer() }
    }

    // FASE 72.2: live proctoring — subscribes to this student's private
    // channel for proctor interventions and starts the liveness heartbeat.
    // Renders as a non-blocking overlay below, never pauses startExamTimer().
    LaunchedEffect(examId, studentId) {
        viewModel.startLiveProctoring(examId, studentId)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopLiveProctoring() }
    }

    // FASE 72.2: CbtAntiCheatEngine.recordViolation() only ever updated local
    // Compose state — no violation (screenshot, root/emulator/USB-debug,
    // window-focus-lost) reached the backend or the proctor's live dashboard
    // unless it escalated all the way to forceCloseExam(). Mirror each new
    // entry to the backend the moment it's recorded. Keyed on violationCount
    // (not violationHistory.size) since unlockExamWithSupervisorPin() also
    // appends to the history without incrementing the count, and must not be
    // reported as a violation.
    LaunchedEffect(antiCheatState.violationCount) {
        antiCheatState.violationHistory.lastOrNull()?.let { record ->
            viewModel.recordViolation(examId, record.type.name)
        }
    }

    val questions = uiState.currentExamQuestions

    // Never fabricate exam content: a graded exam must show real questions or a
    // hard stop, not a silent fallback that could pass fake questions off as real.
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Slate950), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Gold400)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat soal ujian...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                SulaoneErrorBanner(
                    message = uiState.errorMessage ?: "Soal ujian gagal dimuat. Periksa koneksi internet Anda.",
                    onRetry = { viewModel.loadQuestions(examId) },
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        return
    }
    val currentQuestion = questions.getOrNull(uiState.currentQuestionIndex) ?: questions.first()

    val formattedTimer = remember(remainingSeconds) {
        val hours = remainingSeconds / 3600
        val mins = (remainingSeconds % 3600) / 60
        val secs = remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    color = Slate900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Live Anti-Cheat Status Chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            antiCheatState.isExamLocked -> AccentRose.copy(alpha = 0.2f)
                                            antiCheatState.violationCount > 0 -> AccentAmber.copy(alpha = 0.2f)
                                            else -> AccentGreen.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                antiCheatState.isExamLocked -> AccentRose
                                                antiCheatState.violationCount > 0 -> AccentAmber
                                                else -> AccentGreen
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = when {
                                        antiCheatState.isExamLocked -> AccentRose
                                        antiCheatState.violationCount > 0 -> AccentAmber
                                        else -> AccentGreen
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (antiCheatState.violationCount > 0)
                                        "ANTI-CHEAT: ${antiCheatState.violationCount}/${antiCheatState.maxViolationsAllowed}"
                                    else
                                        "ANTI-CHEAT AKTIF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Kiosk Lock Task & Liveness Indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (livenessState.isCameraPulseActive) Emerald700 else Slate800)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (livenessState.isCameraPulseActive) Emerald300 else Emerald400)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (livenessState.isCameraPulseActive) "AUDIT LIVE" else "KIOSK LOCK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Battery & Device Indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate800)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (antiCheatState.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                    contentDescription = null,
                                    tint = if (antiCheatState.batteryLevel < 20) AccentRose else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${antiCheatState.batteryLevel}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }

                            // Timer Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (remainingSeconds < 300) AccentRose else Emerald700)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = formattedTimer,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                // Question Number Selector Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(questions) { index, q ->
                        val isSelected = uiState.currentQuestionIndex == index
                        val isAnswered = uiState.selectedAnswers.containsKey(q.id.toString())

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> Emerald700
                                        isAnswered -> Emerald100
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { viewModel.goToQuestion(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSelected -> Color.White
                                    isAnswered -> Emerald900
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                // Question Content Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    SulaoneCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Text(
                            text = "Soal Nomor ${uiState.currentQuestionIndex + 1} dari ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald700,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Render Question Text with KaTeX formula support
                        CbtLatexMathView(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Render Question Image with Tap-to-Zoom if available
                        if (!currentQuestion.imageUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CbtImageViewer(
                                imageUrl = currentQuestion.imageUrl,
                                maxHeight = 220.dp,
                                allowZoom = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options List (A - E)
                    currentQuestion.options.forEach { option ->
                        val selectedOptionKey = uiState.selectedAnswers[currentQuestion.id.toString()]
                        val isChecked = selectedOptionKey == option.key

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isChecked) 2.dp else 1.dp,
                                    color = if (isChecked) Emerald700 else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(if (isChecked) Emerald50 else MaterialTheme.colorScheme.surface)
                                // FASE 74.1: stable per-option automation hook (A-E),
                                // keyed on the real answer key instead of position so
                                // it survives option shuffling.
                                .testTag("cbt_option_${option.key}")
                                .clickable {
                                    viewModel.selectOption(currentQuestion.id, option.key, examId)
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isChecked) Emerald700 else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.key,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChecked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    // Option Text with LaTeX support
                                    CbtLatexMathView(
                                        text = option.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isChecked) Emerald900 else MaterialTheme.colorScheme.onSurface
                                    )

                                    // Option Image with Tap-to-Zoom if available
                                    if (!option.imageUrl.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CbtImageViewer(
                                            imageUrl = option.imageUrl,
                                            maxHeight = 110.dp,
                                            allowZoom = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Navigation & Submit Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.goToQuestion(uiState.currentQuestionIndex - 1) },
                            enabled = uiState.currentQuestionIndex > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sebelumnya")
                        }

                        if (uiState.currentQuestionIndex == questions.size - 1) {
                            Button(
                                onClick = { showSubmitDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("cbt_collect_button")
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kumpulkan")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.goToQuestion(uiState.currentQuestionIndex + 1) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("cbt_next_button")
                            ) {
                                Text("Selanjutnya")
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // FULL-SCREEN HARDWARE LOCKDOWN OVERLAY (IF EXAM LOCKED)
        // =====================================================================
        if (antiCheatState.isExamLocked) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Slate950,
                                    Slate900,
                                    ObsidianBackground
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, AccentRose)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AccentRose.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AccentRose,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "LEMBAR UJIAN TERKUNCI!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentRose,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Aktivitas mencurigakan terdeteksi melebihi batas toleransi (${antiCheatState.maxViolationsAllowed}x). Akses soal ujian ditangguhkan demi menegakkan kejujuran akademik.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Violation Log Summary
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Catatan Pelanggaran Sistem:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold400
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    antiCheatState.violationHistory.takeLast(3).forEach { v ->
                                        Text(
                                            text = "• ${v.type.title}: ${v.details}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Minta Pengawas / Guru memasukkan PIN Otorisasi:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald300
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = supervisorPinInput,
                                onValueChange = {
                                    supervisorPinInput = it
                                    supervisorPinError = null
                                },
                                placeholder = { Text("Contoh: 195026 / SA1CBT", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                isError = supervisorPinError != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Emerald500,
                                    unfocusedBorderColor = Slate700
                                )
                            )

                            if (supervisorPinError != null) {
                                Text(
                                    text = supervisorPinError ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentRose,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val success = antiCheatEngine.unlockExamWithSupervisorPin(supervisorPinInput)
                                    if (success) {
                                        supervisorPinInput = ""
                                        supervisorPinError = null
                                    } else {
                                        supervisorPinError = "PIN Pengawas salah atau tidak berlaku!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifikasi & Buka Kunci")
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // WARNING MODAL (FOR VIOLATION 1 & 2)
        // =====================================================================
        if (antiCheatState.currentWarningTitle != null && !antiCheatState.isExamLocked) {
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = antiCheatState.currentWarningTitle ?: "Peringatan",
                        fontWeight = FontWeight.Bold,
                        color = AccentAmber
                    )
                },
                text = {
                    Text(
                        text = antiCheatState.currentWarningMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { antiCheatEngine.dismissWarningDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Text("Saya Mengerti, Lanjutkan")
                    }
                }
            )
        }

        // Exit confirmation dialog
        if (showExitWarningDialog) {
            AlertDialog(
                onDismissRequest = { showExitWarningDialog = false },
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = AccentRose) },
                title = { Text("Keluar dari Ujian?") },
                text = {
                    Text("Ujian akan langsung ditutup, jawaban yang sudah terisi (${uiState.selectedAnswers.size} dari ${questions.size} soal) dikirim apa adanya, dan Anda tidak bisa masuk kembali. Keluar juga tercatat untuk pengawas. Jika sudah selesai, gunakan tombol 'Kumpulkan'.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitWarningDialog = false
                            exitAndForceClose("back_button_pressed")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                    ) {
                        Text("Keluar Paksa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitWarningDialog = false }) {
                        Text("Tetap di Ujian")
                    }
                }
            )
        }

        if (showSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Kumpulkan Lembar Jawaban?") },
                text = {
                    Text("Anda telah menjawab ${uiState.selectedAnswers.size} dari ${questions.size} soal. Jawaban yang sudah dikumpulkan tidak dapat diubah kembali.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSubmitDialog = false
                            viewModel.submitExam(examId, vault)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        modifier = Modifier.testTag("cbt_confirm_submit_button")
                    ) {
                        Text("Ya, Kumpulkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitDialog = false }) {
                        Text("Periksa Lagi")
                    }
                }
            )
        }

        // FASE 69.3: honest confirmation after submit — tells the student explicitly
        // when their answers were queued offline instead of silently pretending a
        // normal successful submission happened.
        if (uiState.isSubmitted) {
            AlertDialog(
                onDismissRequest = onNavigateBack,
                modifier = Modifier.testTag(
                    if (uiState.isQueuedOffline) "cbt_submitted_offline_dialog" else "cbt_submitted_online_dialog"
                ),
                title = {
                    Text(if (uiState.isQueuedOffline) "Disimpan Offline" else "Ujian Terkumpul")
                },
                text = {
                    Text(
                        if (uiState.isQueuedOffline) {
                            "Jawaban Anda tersimpan di perangkat karena tidak ada koneksi internet saat mengumpulkan. Jawaban akan otomatis disinkronkan ke server begitu perangkat terhubung kembali."
                        } else {
                            "Lembar jawaban Anda berhasil dikumpulkan ke server."
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text("Selesai")
                    }
                }
            )
        }

        // Proctor Live Intervention Modal (Fase 25)
        uiState.proctorIntervention?.let { cmd ->
            Dialog(
                onDismissRequest = {
                    if (cmd.action != "FORCE_SUBMIT") {
                        viewModel.dismissProctorIntervention()
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = cmd.action != "FORCE_SUBMIT",
                    dismissOnClickOutside = cmd.action != "FORCE_SUBMIT"
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (cmd.action == "FORCE_SUBMIT" || cmd.action == "WARNING_MODAL") AccentRose else Emerald500
                    ),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (cmd.action == "EXTEND_TIME") Icons.Default.Timer else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (cmd.action == "EXTEND_TIME") Emerald400 else AccentRose,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (cmd.action) {
                                "FORCE_SUBMIT" -> "UJIAN DIHENTIKAN PENGAWAS"
                                "EXTEND_TIME" -> "WAKTU TAMBAHAN DIBERIKAN"
                                else -> "PERINGATAN DARI PENGAWAS"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cmd.message ?: "Pengawas melakukan intervensi pada sesi ujian Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (cmd.action == "FORCE_SUBMIT") {
                                    if (activity != null) {
                                        lockTaskManager.stopKioskMode(activity)
                                    }
                                    viewModel.submitExam(examId, vault)
                                    onNavigateBack()
                                } else {
                                    viewModel.dismissProctorIntervention()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cmd.action == "EXTEND_TIME") Emerald700 else AccentRose
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (cmd.action == "FORCE_SUBMIT") "Kumpulkan Sekarang" else "Saya Mengerti")
                        }
                    }
                }
            }
        }
    }
}
