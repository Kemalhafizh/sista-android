package com.sultanagung1.sista.ui.cbt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.data.model.TokenValidationState

/**
 * FASE 67: Overhauled CBT Token Entry Screen.
 * Design-to-code overhaul: Off-white canvas (Slate50), flat 0dp cards,
 * 0.5dp borders, Emerald600 accents, and 48dp WCAG touch targets.
 */
@Composable
fun CbtTokenEntryScreen(
    examId: Long,
    examTitle: String,
    examSubject: String,
    examType: String,
    durationMinutes: Int,
    totalQuestions: Int,
    viewModel: CbtViewModel,
    onTokenValidated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var tokenInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    LaunchedEffect(Unit) {
        viewModel.resetTokenValidationState()
        focusRequester.requestFocus()
    }

    LaunchedEffect(uiState.tokenValidationState) {
        if (uiState.tokenValidationState is TokenValidationState.Success) {
            haptics.success()
            onTokenValidated()
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Token Masuk Ujian",
                subtitle = "Gerbang Keamanan CBT Sultan Agung",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exam Information Card (0dp Flat, 0.5dp Border)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .sulaoneSharedBounds(key = "cbt_exam_card_${examId}"),
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (examType.contains("UTS", ignoreCase = true) || examType.contains("UAS", ignoreCase = true)) {
                                if (isDark) Slate800 else Emerald50
                            } else {
                                if (isDark) Slate800 else Gold50
                            },
                            border = BorderStroke(
                                0.5.dp,
                                if (examType.contains("UTS", ignoreCase = true) || examType.contains("UAS", ignoreCase = true)) Emerald200 else Gold300
                            )
                        ) {
                            Text(
                                text = examType.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (examType.contains("UTS", ignoreCase = true) || examType.contains("UAS", ignoreCase = true)) {
                                    if (isDark) Emerald300 else Emerald700
                                } else {
                                    if (isDark) Gold300 else Gold800
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isDark) Slate400 else Slate500,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$durationMinutes Menit | $totalQuestions Soal",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Slate400 else Slate500
                            )
                        }
                    }

                    Text(
                        text = examTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Slate900
                    )

                    Text(
                        text = examSubject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Slate400 else Slate600
                    )
                }
            }

            // Token Input Section (0dp Flat, 0.5dp Border)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Slate850 else Color.White
                ),
                border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Slate800 else Emerald50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "MASUKKAN TOKEN UJIAN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Slate900
                    )

                    // 5-Minute Token TTL Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Slate800 else Gold50,
                        border = BorderStroke(0.5.dp, Gold400.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isDark) Gold300 else Gold800,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Masa Aktif Token: 5 Menit",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Gold300 else Gold800
                            )
                        }
                    }

                    Text(
                        text = if (examType.contains("UTS", ignoreCase = true) || examType.contains("UAS", ignoreCase = true))
                            "Token 6 digit diberikan oleh Guru Operator Ujian di ruang ujian. Token berlaku selama 5 menit."
                        else
                            "Token 6 digit diberikan langsung oleh Guru Pengampu di kelas Anda. Token berlaku selama 5 menit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate400 else Slate500,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    // 6-digit styled OTP boxes display
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val char = if (i < tokenInput.length) tokenInput[i].toString() else ""
                            val isCurrent = i == tokenInput.length
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Slate800 else Slate100)
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 0.5.dp,
                                        color = if (isCurrent) Emerald600 else (if (isDark) Slate700 else Slate200),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Slate900
                                )
                            }
                        }
                    }

                    // Hidden actual text field to receive IME input
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { input ->
                            if (input.length <= 6) {
                                tokenInput = input.uppercase().filter { it.isLetterOrDigit() }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        label = { Text("Ketik 6 digit token di sini") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (tokenInput.length == 6) {
                                    haptics.tapLight()
                                    viewModel.validateExamToken(examId, tokenInput)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = if (isDark) Slate700 else Slate300
                        )
                    )

                    // Error Message display
                    AnimatedVisibility(visible = uiState.tokenValidationState is TokenValidationState.Error) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = (uiState.tokenValidationState as? TokenValidationState.Error)?.message ?: "Token tidak valid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Submit Token Button
                    Button(
                        onClick = {
                            haptics.tapLight()
                            focusManager.clearFocus()
                            viewModel.validateExamToken(examId, tokenInput)
                        },
                        enabled = tokenInput.length == 6 && uiState.tokenValidationState !is TokenValidationState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .sulaoneInteractiveTouchTarget(48.dp)
                            .springPressable(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (uiState.tokenValidationState is TokenValidationState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VALIDASI & MULAI UJIAN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Security Protocols Card (0dp Flat, 0.5dp Border)
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Ketentuan Integritas Ujian SISTA",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "1. Mobile-Only: Ujian CBT HANYA dapat dikerjakan melalui aplikasi resmi SISTA Android pada ponsel Anda, bukan melalui browser website.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate400 else Slate500,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "2. Auto-Close on Exit: Jika Anda meminimalkan aplikasi, membuka aplikasi lain, split-screen, atau menekan tombol Kembali, sesi ujian akan otomatis DITUTUP PAKSA dan lembar jawaban Anda di-snapshot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate400 else Slate500,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "3. Reset Token: Siswa yang sesi ujiannya tertutup paksa wajib melapor ke Pengawas/Operator untuk di-reset dan meminta token baru sebelum dapat melanjutkan ujian.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate400 else Slate500,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
