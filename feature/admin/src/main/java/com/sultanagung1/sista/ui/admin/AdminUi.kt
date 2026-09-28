package com.sultanagung1.sista.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.EmergencyBroadcastData
import com.sultanagung1.sista.data.model.PendingApprovalItem
import java.time.OffsetDateTime
import java.util.Locale

/** Confirms a decision; the note goes with it (a reason when rejecting). */
@Composable
internal fun DecisionDialog(
    item: PendingApprovalItem,
    approve: Boolean,
    onConfirm: (notes: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var notes by rememberSaveable { mutableStateOf("") }
    val lastStep = item.totalSteps?.let { item.currentStep >= it } ?: false
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (approve) "Setujui pengajuan?" else "Tolak pengajuan?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    when {
                        !approve -> "${item.typeName} dari ${item.requesterName} akan ditolak dan alurnya berhenti."
                        lastStep -> "${item.typeName} dari ${item.requesterName} akan disetujui. Ini langkah terakhir."
                        else -> "${item.typeName} dari ${item.requesterName} akan diteruskan ke penyetuju berikutnya."
                    },
                    style = SistaTheme.typography.bodyMedium,
                )
                SistaTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = if (approve) "Catatan (opsional)" else "Alasan (opsional)",
                    singleLine = false,
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(notes.trim().ifBlank { null }) }) { Text(if (approve) "Setujui" else "Tolak") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

/** Composes and confirms the emergency broadcast, then shows what was sent. */
@Composable
internal fun EmergencyBroadcastDialog(
    sending: Boolean,
    sent: EmergencyBroadcastData?,
    error: String?,
    onSend: (title: String, message: String, location: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    if (sent != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Siaran terkirim") },
            text = { Text("\"${sent.title}\" sudah tampil di aplikasi seluruh warga sekolah.", style = SistaTheme.typography.bodyMedium) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
        )
        return
    }
    AlertDialog(
        onDismissRequest = { if (!sending) onDismiss() },
        title = { Text("Kirim siaran darurat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    "Pesan ini langsung tampil di aplikasi seluruh siswa, guru, dan wali murid. Gunakan hanya untuk keadaan darurat.",
                    style = SistaTheme.typography.bodyMedium,
                )
                SistaTextField(value = title, onValueChange = { title = it.take(255) }, label = "Judul", enabled = !sending)
                SistaTextField(value = message, onValueChange = { message = it.take(2000) }, label = "Pesan", singleLine = false, minLines = 3, enabled = !sending)
                SistaTextField(value = location, onValueChange = { location = it.take(255) }, label = "Lokasi (opsional)", enabled = !sending)
                error?.let { Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSend(title.trim(), message.trim(), location.trim().ifBlank { null }) },
                enabled = !sending && title.isNotBlank() && message.isNotBlank(),
            ) { Text(if (sending) "Mengirim…" else "Kirim sekarang") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !sending) { Text("Batal") } },
    )
}

private val ROLE_LABELS = mapOf(
    "guru" to "Guru", "teacher" to "Guru", "bk" to "BK", "wali_kelas" to "Wali kelas", "staf_tu" to "Staf TU",
    "admin" to "Admin", "kepsek" to "Kepala sekolah", "kepala_sekolah" to "Kepala sekolah",
    "waka_kurikulum" to "Waka kurikulum", "waka_kesiswaan" to "Waka kesiswaan", "student" to "Siswa", "siswa" to "Siswa",
)

internal fun roleLabel(role: String): String = ROLE_LABELS[role.lowercase()] ?: role.replace('_', ' ').replaceFirstChar { it.uppercase() }

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

/** "Lewat batas 28 Sep" / "Batas 30 Sep"; null without a due date. */
internal fun dueLabel(item: PendingApprovalItem, nowMillis: Long): Pair<String, StatusTone>? {
    val due = item.dueAt?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() } ?: return null
    val text = "${due.dayOfMonth} ${MONTHS[due.monthValue - 1]}"
    val overdue = item.isOverdue || due.toInstant().toEpochMilli() < nowMillis
    return if (overdue) "Lewat batas $text" to StatusTone.Danger else "Batas $text" to StatusTone.Neutral
}

/** 96.5 → "96,5", 90.0 → "90". */
internal fun decimal(value: Double): String {
    val rounded = Math.round(value * 10) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(Locale.US, "%.1f", rounded).replace('.', ',')
}

/** Rp 950.000 · Rp 12,5 jt · Rp 1,2 M — short enough for a tile. */
internal fun rupiahShort(amount: Double): String = when {
    amount >= 1_000_000_000 -> "Rp ${decimal(amount / 1_000_000_000)} M"
    amount >= 1_000_000 -> "Rp ${decimal(amount / 1_000_000)} jt"
    else -> "Rp " + String.format(Locale.US, "%,.0f", amount).replace(',', '.')
}

/** 12480 → "12.480". */
internal fun thousands(value: Int): String = String.format(Locale.US, "%,d", value).replace(',', '.')
