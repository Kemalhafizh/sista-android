package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SulaoneDropdown(
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    options: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    searchEnabled: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredOptions = if (searchEnabled && searchQuery.isNotEmpty()) {
        options.filter { it.contains(searchQuery, ignoreCase = true) }
    } else {
        options
    }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        SulaoneTextField(
            value = selectedValue,
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (searchEnabled) {
                SulaoneTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Cari...",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                        searchQuery = ""
                    }
                )
            }
            if (filteredOptions.isEmpty() && searchEnabled) {
                DropdownMenuItem(
                    text = { Text("Tidak ditemukan", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}
