package com.sultanagung1.sista.ui.portal

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.data.model.EnterpriseModuleItem
import com.sultanagung1.sista.data.model.EnterpriseModuleCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseCatalogScreen(
    viewModel: ModuleCatalogViewModel = hiltViewModel(),

    userRole: String? = "student",
    onNavigateBack: () -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    val isUserExecutive = userRole?.contains("admin", true) == true ||
            userRole?.contains("superadmin", true) == true ||
            userRole?.contains("kepsek", true) == true ||
            userRole?.contains("principal", true) == true
    val isUserTeacherOrAbove = isUserExecutive ||
            userRole?.contains("teacher", true) == true ||
            userRole?.contains("guru", true) == true
    val isUserParentOrAbove = isUserExecutive ||
            userRole?.contains("parent", true) == true ||
            userRole?.contains("ortu", true) == true

    val categories = remember(userRole) {
        val list = mutableListOf("Semua")
        if (isUserExecutive) {
            list.add("Rahasia Pimpinan")
        }
        if (isUserTeacherOrAbove) {
            list.add("Khusus Guru")
        }
        if (isUserParentOrAbove) {
            list.add("Wali Murid")
        }
        list.addAll(
            listOf(
                "Khusus Siswa",
                "Akademik & LMS",
                "Presensi & IoT",
                "Kecerdasan Buatan (AI Edukasi)",
                "Kesiswaan & Ibadah",
                "Keuangan & Tata Usaha"
            )
        )
        list
    }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    val favoriteSet by viewModel.favoriteIds.collectAsState()

    val modules = EnterpriseModuleCatalog.ALL

    val authorizedModules = remember(userRole) {
        modules.filter { item ->
            val isExecutiveOnly = item.route == "executive_analytics" || item.route == "admin_dashboard" || item.id == "m25"
            val isTeacherOnly = item.route.startsWith("teacher") || item.route == "question_bank" || item.route == "auto_generate_exam" || item.route == "class_analytics" || item.id == "m23" || item.route == "digital_signature" || item.id == "m21"
            val isParentOnly = item.id == "m24" || item.route == "child_progress"

            when {
                isExecutiveOnly -> isUserExecutive
                isTeacherOnly -> isUserTeacherOrAbove
                isParentOnly -> isUserParentOrAbove
                else -> true
            }
        }
    }

    val filteredModules = authorizedModules.filter { item ->
        val isExecutiveOnly = item.route == "executive_analytics" || item.route == "admin_dashboard" || item.id == "m25"
        val isTeacherOnly = item.route.startsWith("teacher") || item.route == "question_bank" || item.route == "auto_generate_exam" || item.route == "class_analytics" || item.id == "m23" || item.route == "digital_signature" || item.id == "m21"
        val isParentOnly = item.id == "m24" || item.route == "child_progress"

        val matchesCategory = when (selectedCategory) {
            "Semua" -> true
            "Khusus Siswa" -> !isExecutiveOnly && !isTeacherOnly && !isParentOnly
            "Khusus Guru" -> isTeacherOnly
            "Rahasia Pimpinan" -> isExecutiveOnly
            "Wali Murid" -> isParentOnly
            else -> item.category == selectedCategory
        }

        val matchesQuery = if (searchQuery.isBlank()) true else {
            item.title.contains(searchQuery, ignoreCase = true) || item.subtitle.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Direktori Modul SISTA",
                subtitle = "Ekosistem Terintegrasi (${authorizedModules.size} Modul Aktif)",
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
            // Live Search Bar & Favorites Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari modul (contoh: CBT, Presensi, Rapor)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald700) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onNavigateRoute("module_favorites") },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold100)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Favorit", tint = Gold800)
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Modules List (Strictly authorized only - unauthorized modules completely absent)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredModules, key = { it.id }) { item ->
                    val isFav = favoriteSet.contains(item.id)

                    SulaoneCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp,
                        onClick = {
                            onNavigateRoute(item.route)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald50),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "F${item.phaseNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald800
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(item.id)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Pin Favorite",
                                    tint = if (isFav) Gold600 else Slate400
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Slate400
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

