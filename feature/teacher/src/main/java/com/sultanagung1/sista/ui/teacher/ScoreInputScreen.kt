package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.AssessmentScoreRow
import com.sultanagung1.sista.data.model.AssessmentScoreSheet
import com.sultanagung1.sista.data.model.ScoreSheetRules

/**
 * Scores for one daily assessment. The class and its stored scores come from
 * `assessments/{id}/scores`; only changed scores are sent to `batch-scores`,
 * so a student left blank is never saved as 0.
 */
@Composable
fun ScoreInputScreen(
    assessmentId: Long,
    viewModel: DailyAssessmentViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(assessmentId) { viewModel.loadScoreSheet(assessmentId) }

    ScoreInputContent(
        sheet = uiState.sheet,
        loading = uiState.isLoadingSheet,
        loadError = uiState.sheetError,
        edits = uiState.edits,
        submitting = uiState.isSubmitting,
        errorMessage = uiState.errorMessage,
        successMessage = uiState.successMessage,
        onEdit = viewModel::editScore,
        onSave = viewModel::saveScores,
        onAutoRemedial = { viewModel.triggerAutoRemedial(assessmentId) },
        onRetry = { viewModel.loadScoreSheet(assessmentId) },
        onDismissMessage = viewModel::clearMessages,
        onNavigateBack = onNavigateBack,
    )
}

/** The score sheet without a ViewModel, for previews and screenshots. */
@Composable
fun ScoreInputContent(
    sheet: AssessmentScoreSheet?,
    loading: Boolean,
    loadError: String?,
    edits: Map<Long, String>,
    submitting: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onEdit: (studentId: Long, text: String) -> Unit,
    onSave: () -> Unit,
    onAutoRemedial: () -> Unit,
    onRetry: () -> Unit,
    onDismissMessage: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmRemedial by rememberSaveable { mutableStateOf(false) }
    val header = sheet?.assessment
    val summary = sheet?.let { ScoreSheetRules.summary(it.students, edits, it.assessment.kkm, it.assessment.maxScore) }
    // Remedial is assigned from what is stored on the server, not from unsaved edits.
    val storedBelowKkm = sheet?.students?.count { row -> row.score?.let { it < sheet.assessment.kkm } == true } ?: 0

    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = "Input nilai",
                    subtitle = header?.title,
                    onBack = onNavigateBack,
                    actions = {
                        if (sheet != null) {
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Outlined.MoreVert, contentDescription = "Menu lainnya")
                                }
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Tugaskan remedial") },
                                        enabled = !submitting,
                                        onClick = {
                                            menuOpen = false
                                            confirmRemedial = true
                                        },
                                    )
                                }
                            }
                        }
                    },
                )
            },
            bottomBar = {
                if (sheet != null && summary != null) {
                    SaveBar(changed = summary.changed, invalid = summary.invalid, submitting = submitting, onSave = onSave)
                }
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            when {
                sheet == null && loading -> SkeletonList(Modifier.padding(padding), rows = 6)
                sheet == null -> ErrorState(
                    title = "Daftar nilai belum bisa dimuat",
                    body = loadError ?: "Coba muat ulang.",
                    onRetry = onRetry,
                    modifier = Modifier
                        .padding(padding)
                        .padding(Spacing.screen),
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding(),
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    item(key = "header") {
                        Text(
                            listOfNotNull(
                                header?.subjectName,
                                header?.classroomName?.let { "Kelas $it" },
                                header?.assessmentDate?.let(::journalDate),
                                "KKM ${ScoreSheetRules.format(sheet.assessment.kkm)}",
                                "Maks ${ScoreSheetRules.format(ScoreSheetRules.ceiling(sheet.assessment.maxScore))}",
                            ).joinToString(" · "),
                            style = SistaTheme.typography.bodyMedium,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                    if (summary != null) {
                        item(key = "stats") {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                StatTile(label = "Sudah dinilai", value = "${summary.filled}/${summary.total}", modifier = Modifier.weight(1f))
                                StatTile(
                                    label = "Di bawah KKM",
                                    value = summary.belowKkm.toString(),
                                    tone = if (summary.belowKkm > 0) StatusTone.Warning else StatusTone.Neutral,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    if (successMessage != null) {
                        item(key = "success") {
                            InlineBanner(message = successMessage, tone = StatusTone.Success, onDismiss = onDismissMessage)
                        }
                    }
                    if (errorMessage != null) {
                        item(key = "error") {
                            InlineBanner(message = errorMessage, tone = StatusTone.Danger, onDismiss = onDismissMessage)
                        }
                    }
                    if (loadError != null) {
                        item(key = "stale") {
                            InlineBanner(
                                message = "Gagal memperbarui. $loadError",
                                tone = StatusTone.Warning,
                                actionLabel = "Coba lagi",
                                onAction = onRetry,
                            )
                        }
                    }
                    if (sheet.students.isEmpty()) {
                        item(key = "empty") {
                            InlineBanner(message = "Belum ada siswa di kelas ini.", tone = StatusTone.Info)
                        }
                    } else {
                        item(key = "roster") {
                            SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs)) {
                                Column {
                                    sheet.students.forEachIndexed { index, row ->
                                        if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                                        ScoreRow(
                                            number = index + 1,
                                            row = row,
                                            text = edits[row.studentId] ?: ScoreSheetRules.textOf(row.score),
                                            edited = row.studentId in edits,
                                            kkm = sheet.assessment.kkm,
                                            maxScore = sheet.assessment.maxScore,
                                            enabled = !submitting,
                                            onEdit = { onEdit(row.studentId, it) },
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

    if (confirmRemedial && sheet != null) {
        val unsaved = (summary?.changed ?: 0) > 0
        AlertDialog(
            onDismissRequest = { confirmRemedial = false },
            title = { Text("Tugaskan remedial?") },
            text = {
                Text(
                    when {
                        unsaved -> "Ada nilai yang belum disimpan. Simpan dulu, karena remedial ditugaskan dari nilai yang tersimpan."
                        storedBelowKkm == 0 -> "Tidak ada nilai tersimpan di bawah KKM ${ScoreSheetRules.format(sheet.assessment.kkm)}."
                        else -> "$storedBelowKkm siswa dengan nilai di bawah KKM ${ScoreSheetRules.format(sheet.assessment.kkm)} akan ditugaskan remedial."
                    },
                )
            },
            confirmButton = {
                if (!unsaved && storedBelowKkm > 0) {
                    TextButton(onClick = {
                        confirmRemedial = false
                        onAutoRemedial()
                    }) { Text("Tugaskan") }
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemedial = false }) { Text(if (unsaved || storedBelowKkm == 0) "Tutup" else "Batal") }
            },
        )
    }
}

@Composable
private fun ScoreRow(
    number: Int,
    row: AssessmentScoreRow,
    text: String,
    edited: Boolean,
    kkm: Double,
    maxScore: Double,
    enabled: Boolean,
    onEdit: (String) -> Unit,
) {
    val error = if (edited) ScoreSheetRules.errorOf(text, maxScore, row.score) else null
    val value = if (error == null) ScoreSheetRules.parse(text) else null
    Row(
        modifier = Modifier.padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$number",
            style = SistaTheme.typography.labelMedium,
            color = SistaTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(24.dp),
        )
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.name,
                    style = SistaTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (edited && error == null) {
                    Spacer(Modifier.width(Spacing.xs))
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(SistaTheme.colors.primary, CircleShape)
                            .semantics { contentDescription = "Belum disimpan" },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (row.nis ?: row.nisn)?.let { "NIS $it" } ?: "Tanpa NIS",
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
                if (value != null && value < kkm) {
                    Spacer(Modifier.width(Spacing.sm))
                    StatusPill("Di bawah KKM", StatusTone.Warning)
                }
            }
        }
        Spacer(Modifier.width(Spacing.md))
        OutlinedTextField(
            value = text,
            onValueChange = { input -> onEdit(input.filter { it.isDigit() || it == ',' || it == '.' }.take(6)) },
            modifier = Modifier
                .width(84.dp)
                .semantics { contentDescription = "Nilai ${row.name}" },
            enabled = enabled,
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it, maxLines = 1) } },
            placeholder = { Text("–") },
            textStyle = SistaTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            shape = SistaTheme.shapes.medium,
        )
    }
}

@Composable
private fun SaveBar(changed: Int, invalid: Int, submitting: Boolean, onSave: () -> Unit) {
    Surface(color = SistaTheme.colors.surface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screen, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                when {
                    invalid > 0 -> "$invalid nilai belum valid"
                    changed > 0 -> "$changed nilai belum disimpan"
                    else -> "Semua nilai tersimpan"
                },
                style = SistaTheme.typography.bodyMedium,
                color = if (invalid > 0) SistaTheme.colors.error else SistaTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            SistaButton(
                text = if (changed > 0) "Simpan ($changed)" else "Simpan",
                onClick = onSave,
                variant = ButtonVariant.Primary,
                leadingIcon = Icons.Outlined.Save,
                loading = submitting,
                enabled = changed > 0 && invalid == 0 && !submitting,
            )
        }
    }
}
