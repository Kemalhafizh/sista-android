package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SulaoneDatePicker(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    title: String = "Pilih Tanggal",
    initialSelectedDateMillis: Long? = null
) {
    if (!isVisible) return

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    val selectedDateFormatted by remember {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let {
                val localDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                val gregorianFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
                
                // Simple Hijri approximation for UI showcase
                val hijriYear = localDate.year - 579
                val hijriFmt = "${localDate.dayOfMonth} Bulan Hijriyah $hijriYear H"
                
                "${localDate.format(gregorianFmt)}\n$hijriFmt"
            }
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }
            ) {
                Text(
                    text = "Simpan",
                    color = Emerald700,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Emerald700,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Emerald700,
                    todayContentColor = Emerald700
                )
            )

            if (selectedDateFormatted != null) {
                Text(
                    text = selectedDateFormatted!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Emerald800,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
