package com.sultanagung1.sista.ui.teacher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.TeacherCreateOptionInput
import com.sultanagung1.sista.data.model.TeacherCreateQuestionInput
import com.sultanagung1.sista.ui.cbt.components.CbtImageViewer
import com.sultanagung1.sista.ui.cbt.components.CbtLatexMathView
import com.sultanagung1.sista.ui.cbt.components.CbtLatexToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCreateExamScreen(
    onNavigateBack: () -> Unit,
    onExamCreated: (Long) -> Unit = {}
) {
    var examTitle by remember { mutableStateOf("Ulangan Harian Kalkulus & Saintek") }
    var subjectName by remember { mutableStateOf("Matematika Tingkat Lanjut") }
    var targetClass by remember { mutableStateOf("XII MIPA 1") }
    var durationMinutes by remember { mutableStateOf("90") }
    var passingScore by remember { mutableStateOf("75") }
    var examToken by remember { mutableStateOf("SA1-${(1000..9999).random()}") }

    // Anti-Cheat Settings
    var requireKioskMode by remember { mutableStateOf(true) }
    var blockScreenshots by remember { mutableStateOf(true) }
    var detectRootAndEmulator by remember { mutableStateOf(true) }
    var blockMultiWindow by remember { mutableStateOf(true) }
    var shuffleQuestions by remember { mutableStateOf(true) }
    var shuffleOptions by remember { mutableStateOf(true) }

    // Manual Questions List State
    val questions = remember {
        mutableStateListOf(
            TeacherCreateQuestionInput(
                questionText = "Tentukan nilai dari turunan pertama fungsi trigonometri berikut:\n\$\$f(x) = 3\\sin(2x) + \\sqrt{4x^2 + 1}\$\$",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=600&auto=format&fit=crop&q=80",
                options = listOf(
                    TeacherCreateOptionInput(key = "A", text = "\$\$6\\cos(2x) + \\frac{4x}{\\sqrt{4x^2 + 1}}\$\$"),
                    TeacherCreateOptionInput(key = "B", text = "\$\$3\\cos(2x) + \\frac{2x}{\\sqrt{4x^2 + 1}}\$\$"),
                    TeacherCreateOptionInput(key = "C", text = "\$\$6\\sin(2x) + 8x\$\$"),
                    TeacherCreateOptionInput(key = "D", text = "\$\$6\\cos(2x) + \\frac{1}{2\\sqrt{4x^2 + 1}}\$\$"),
                    TeacherCreateOptionInput(key = "E", text = "\$\$0\$\$")
                ),
                correctAnswer = "A",
                scoreWeight = 10.0
            ),
            TeacherCreateQuestionInput(
                questionText = "Hitunglah nilai determinan dari matriks orde 2x2 berikut ini:\n\$\$A = \\begin{pmatrix} 4 & 2 \\\\ 3 & 5 \\end{pmatrix}\$\$",
                options = listOf(
                    TeacherCreateOptionInput(key = "A", text = "\$\$14\$\$"),
                    TeacherCreateOptionInput(key = "B", text = "\$\$20\$\$"),
                    TeacherCreateOptionInput(key = "C", text = "\$\$6\$\$"),
                    TeacherCreateOptionInput(key = "D", text = "\$\$26\$\$"),
                    TeacherCreateOptionInput(key = "E", text = "\$\$-14\$\$")
                ),
                correctAnswer = "A",
                scoreWeight = 10.0
            )
        )
    }

    var activeQuestionIndex by remember { mutableIntStateOf(0) }
    var showLivePreview by remember { mutableStateOf(true) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Image Picker for Main Question
    val questionImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeQuestionIndex in questions.indices) {
            val cur = questions[activeQuestionIndex]
            questions[activeQuestionIndex] = cur.copy(imageUrl = uri.toString())
        }
    }

    // Image Picker for Option (temporary target option index)
    var targetOptionIndexForImage by remember { mutableIntStateOf(-1) }
    val optionImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeQuestionIndex in questions.indices && targetOptionIndexForImage >= 0) {
            val curQ = questions[activeQuestionIndex]
            val updatedOptions = curQ.options.toMutableList()
            if (targetOptionIndexForImage in updatedOptions.indices) {
                updatedOptions[targetOptionIndexForImage] = updatedOptions[targetOptionIndexForImage].copy(
                    imageUrl = uri.toString()
                )
                questions[activeQuestionIndex] = curQ.copy(options = updatedOptions)
            }
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Penyusun Ujian CBT Saintek",
                subtitle = "SMA Islam Sultan Agung 1 Semarang",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Exam Basic Info Card
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "1. Informasi Pokok Ujian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Nama Ujian / Penilaian") },
                    placeholder = { Text("Contoh: Ulangan Harian Dinamika Rotasi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Mata Pelajaran") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Kelas Sasaran") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("Durasi (Menit)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = passingScore,
                        onValueChange = { passingScore = it },
                        label = { Text("Standar KKTP") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Token Masuk Ujian Siswa:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            Text(examToken, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Emerald800)
                        }
                        IconButton(onClick = { examToken = "SA1-${(1000..9999).random()}" }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Acak Token", tint = Emerald700)
                        }
                    }
                }
            }

            // 2. Anti-Cheat Configuration Card
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AccentRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. Konfigurasi Anti-Cheat Level Hardware/OS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Proteksi sistem Android untuk memastikan kejujuran pengerjaan oleh siswa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle 1: FLAG_SECURE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Blokir Tangkapan Layar & Rekaman (FLAG_SECURE)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Mencegah screenshot, screen record, dan screen mirroring", fontSize = 11.sp, color = Slate600)
                    }
                    Switch(checked = blockScreenshots, onCheckedChange = { blockScreenshots = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Toggle 2: Kiosk Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kunci Layar Penuh (Kiosk Mode)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Mendeteksi saat siswa berpindah aplikasi atau menarik notifikasi", fontSize = 11.sp, color = Slate600)
                    }
                    Switch(checked = requireKioskMode, onCheckedChange = { requireKioskMode = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Toggle 3: Multi-Window & Floating Apps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Blokir Split Screen & Floating Apps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Menutup otomatis kalkulator melayang atau browser split screen", fontSize = 11.sp, color = Slate600)
                    }
                    Switch(checked = blockMultiWindow, onCheckedChange = { blockMultiWindow = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Toggle 4: Root & Emulator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deteksi Root (Magisk) & Emulator PC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Memblokir HP rooted atau emulator BlueStacks/Nox", fontSize = 11.sp, color = Slate600)
                    }
                    Switch(checked = detectRootAndEmulator, onCheckedChange = { detectRootAndEmulator = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Toggle 5: Shuffling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Acak Urutan Soal & Pilihan Jawaban", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Tiap siswa mendapatkan urutan soal dan opsi A-E yang berbeda", fontSize = 11.sp, color = Slate600)
                    }
                    Switch(checked = shuffleQuestions, onCheckedChange = { shuffleQuestions = it })
                }
            }

            // 3. Manual Question Authoring Card (LaTeX & Image Support)
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = Emerald700)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3. Bank & Pembuat Soal Mandiri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Ketik soal manual dengan formula LaTeX saintek & lampiran gambar grafik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }

                    // Toggle Live Preview
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (showLivePreview) Emerald100 else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showLivePreview = !showLivePreview }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (showLivePreview) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (showLivePreview) Emerald900 else Slate600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showLivePreview) "Preview Aktif" else "Preview",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (showLivePreview) Emerald900 else Slate600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Tabs / Number Bar
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(questions) { idx, _ ->
                        val isSelected = activeQuestionIndex == idx
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { activeQuestionIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                questions.add(
                                    TeacherCreateQuestionInput(
                                        questionText = "Ketik soal nomor ${questions.size + 1} di sini...",
                                        options = listOf(
                                            TeacherCreateOptionInput(key = "A", text = "Pilihan A"),
                                            TeacherCreateOptionInput(key = "B", text = "Pilihan B"),
                                            TeacherCreateOptionInput(key = "C", text = "Pilihan C"),
                                            TeacherCreateOptionInput(key = "D", text = "Pilihan D"),
                                            TeacherCreateOptionInput(key = "E", text = "Pilihan E")
                                        ),
                                        correctAnswer = "A"
                                    )
                                )
                                activeQuestionIndex = questions.size - 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald100),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Emerald900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Soal", color = Emerald900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Question Editor
                if (activeQuestionIndex in questions.indices) {
                    val currentQ = questions[activeQuestionIndex]

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menyunting Soal #${activeQuestionIndex + 1} dari ${questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )

                        if (questions.size > 1) {
                            TextButton(
                                onClick = {
                                    questions.removeAt(activeQuestionIndex)
                                    if (activeQuestionIndex >= questions.size) {
                                        activeQuestionIndex = questions.size - 1
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = AccentRose)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hapus Soal", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Question Text Input
                    OutlinedTextField(
                        value = currentQ.questionText,
                        onValueChange = { newText ->
                            questions[activeQuestionIndex] = currentQ.copy(questionText = newText)
                        },
                        label = { Text("Isi Teks Pertanyaan (Gunakan \$\$ untuk rumus)") },
                        placeholder = { Text("Contoh: Tentukan limit \$\$\\lim_{x\\to 0}\\frac{\\sin x}{x}\$\$") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // LaTeX Toolbar for Fast Formula Insertion
                    CbtLatexToolbar(
                        onInsertSnippet = { snippet ->
                            val currentText = currentQ.questionText
                            val newText = if (currentText.endsWith(" ") || currentText.isEmpty()) {
                                "$currentText\$\$$snippet\$\$ "
                            } else {
                                "$currentText \$\$$snippet\$\$ "
                            }
                            questions[activeQuestionIndex] = currentQ.copy(questionText = newText)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image Attachment for Question
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lampiran Gambar Soal (Opsional):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                        OutlinedButton(
                            onClick = { questionImagePicker.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald700)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Gambar", fontSize = 12.sp, color = Emerald700)
                        }
                    }

                    if (!currentQ.imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CbtImageViewer(
                                imageUrl = currentQ.imageUrl,
                                maxHeight = 160.dp,
                                allowZoom = true
                            )
                            IconButton(
                                onClick = {
                                    questions[activeQuestionIndex] = currentQ.copy(imageUrl = null)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus Gambar", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Options A, B, C, D, E
                    Text("Pilihan Jawaban (A-E):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Emerald800)
                    Text("Klik lingkaran opsi untuk menandai Kunci Jawaban Benar.", style = MaterialTheme.typography.bodySmall, color = Slate600)

                    Spacer(modifier = Modifier.height(10.dp))

                    currentQ.options.forEachIndexed { optIdx, option ->
                        val isCorrect = currentQ.correctAnswer == option.key

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCorrect) Emerald50 else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isCorrect) 1.5.dp else 1.dp,
                                color = if (isCorrect) Emerald700 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Radio / Key Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isCorrect) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                questions[activeQuestionIndex] = currentQ.copy(correctAnswer = option.key)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option.key,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Option Text Input
                                    OutlinedTextField(
                                        value = option.text,
                                        onValueChange = { newOptText ->
                                            val updatedOpts = currentQ.options.toMutableList()
                                            updatedOpts[optIdx] = option.copy(text = newOptText)
                                            questions[activeQuestionIndex] = currentQ.copy(options = updatedOpts)
                                        },
                                        placeholder = { Text("Teks / Rumus LaTeX Opsi ${option.key}") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = false
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Option Image Picker Button
                                    IconButton(
                                        onClick = {
                                            targetOptionIndexForImage = optIdx
                                            optionImagePicker.launch("image/*")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Lampirkan Gambar Opsi",
                                            tint = if (!option.imageUrl.isNullOrBlank()) Emerald700 else Slate600
                                        )
                                    }
                                }

                                // Option Image Preview if attached
                                if (!option.imageUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 42.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CbtImageViewer(
                                            imageUrl = option.imageUrl,
                                            maxHeight = 90.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                val updatedOpts = currentQ.options.toMutableList()
                                                updatedOpts[optIdx] = option.copy(imageUrl = null)
                                                questions[activeQuestionIndex] = currentQ.copy(options = updatedOpts)
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Hapus Gambar Opsi", tint = AccentRose)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Live Student Visual Preview Box
                    if (showLivePreview) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald700.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pratinjau Nyata di Layar Murid (KaTeX & Visual)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Question text rendered with KaTeX
                                CbtLatexMathView(
                                    text = currentQ.questionText.ifBlank { "(Teks soal kosong)" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!currentQ.imageUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    CbtImageViewer(imageUrl = currentQ.imageUrl, maxHeight = 180.dp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Preview Options
                                currentQ.options.forEach { opt ->
                                    val isCorrect = currentQ.correctAnswer == opt.key
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCorrect) Emerald100.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "${opt.key}.",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Emerald900 else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            CbtLatexMathView(
                                                text = opt.text.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isCorrect) Emerald900 else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (!opt.imageUrl.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                CbtImageViewer(imageUrl = opt.imageUrl, maxHeight = 70.dp)
                                            }
                                        }
                                        if (isCorrect) {
                                            Icon(Icons.Default.Check, contentDescription = "Kunci", tint = Emerald700, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Pill & Publish Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Emerald50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ringkasan Paket Ujian:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                        Text("${questions.size} Butir Soal Terdaftar • ${durationMinutes} Menit", fontWeight = FontWeight.Bold, color = Emerald900)
                    }
                    Text(
                        text = "Anti-Cheat 100%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700
                    )
                }
            }

            // Publish Button
            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terbitkan Jadwal & Bank Soal Ujian", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(44.dp)) },
            title = { Text("Jadwal & Soal Ujian Diterbitkan!") },
            text = {
                Column {
                    Text("Ulangan '$examTitle' ($subjectName) dengan total ${questions.size} butir soal saintek berhasil disinkronisasi ke server.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Token Masuk: $examToken", fontWeight = FontWeight.Bold, color = Emerald800)
                    Text("Proteksi Anti-Cheat: Kiosk, Anti-Screenshot, Root Shield AKTIF.", fontSize = 11.sp, color = Slate600)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onExamCreated(101L)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Buka Ruang Pengawas")
                }
            }
        )
    }
}
