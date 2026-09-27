package com.sultanagung1.sista.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.QuestionBankCategory

@Composable
fun QuestionBankScreen(
    viewModel: QuestionBankViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAutoGenerate: () -> Unit,
    onNavigateToManualCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowWidthClass = LocalWindowWidthSizeClass.current
    var selectedCategory by remember { mutableStateOf<QuestionBankCategory?>(null) }

    // Auto-select kategori pertama di tablet
    LaunchedEffect(windowWidthClass, uiState.categories) {
        if (windowWidthClass != WindowWidthSizeClass.Compact && selectedCategory == null && uiState.categories.isNotEmpty()) {
            selectedCategory = uiState.categories.first()
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Bank Soal Terpusat Guru",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToAutoGenerate) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Auto Generate", tint = Gold700)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToManualCreate,
                containerColor = Emerald700,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Buat Soal Baru") }
            )
        }
    ) { paddingValues ->
        if (windowWidthClass == WindowWidthSizeClass.Compact) {
            // Mode Single-Pane (Smartphone)
            QuestionCategoryListContent(
                categories = uiState.categories,
                isLoading = uiState.isLoading,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                onNavigateToAutoGenerate = onNavigateToAutoGenerate,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            // Mode Dual-Pane (Tablet & Foldable — FASE 55.1)
            SulaoneListDetailPaneScaffold(
                selectedItem = selectedCategory ?: uiState.categories.firstOrNull() ?: QuestionBankCategory(id = 0L, name = "", gradeLevel = "X"),
                onSelectItem = { selectedCategory = it },
                listPaneWidth = 380.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                listPane = {
                    QuestionCategoryListContent(
                        categories = uiState.categories,
                        isLoading = uiState.isLoading,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        onNavigateToAutoGenerate = onNavigateToAutoGenerate,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                detailPane = { category, _ ->
                    if (category != null && category.id != 0L) {
                        QuestionCategoryDetailContent(
                            category = category,
                            onNavigateToAutoGenerate = onNavigateToAutoGenerate,
                            onNavigateToManualCreate = onNavigateToManualCreate,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EmptyQuestionBankPlaceholder()
                    }
                },
                emptyDetailPlaceholder = {
                    EmptyQuestionBankPlaceholder()
                }
            )
        }
    }
}

@Composable
private fun QuestionCategoryListContent(
    categories: List<QuestionBankCategory>,
    isLoading: Boolean,
    selectedCategory: QuestionBankCategory?,
    onSelectCategory: (QuestionBankCategory) -> Unit,
    onNavigateToAutoGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Banner Auto-Generate
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToAutoGenerate() },
                colors = CardDefaults.cardColors(containerColor = Emerald50)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(Emerald700, Emerald900))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Generate Ujian CBT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                        Text(
                            text = "Pilih kriteria CP/TP & distribusi tingkat kesulitan secara otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald700
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Emerald700)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kategori Kompetensi & CP/TP (${categories.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald700)
                }
            }
        } else {
            items(categories) { category ->
                val isSelected = selectedCategory?.id == category.id
                CategoryCardItem(
                    category = category,
                    isSelected = isSelected,
                    onClick = { onSelectCategory(category) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CategoryCardItem(
    category: QuestionBankCategory,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    SulaoneCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, Emerald600, RoundedCornerShape(16.dp))
                else Modifier
            ),
        elevation = if (isSelected) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Emerald600 else Emerald100
                    ) {
                        Text(
                            text = "Fase ${category.gradeLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Emerald800,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    val curriculumRef = category.curriculumRef
                    if (!curriculumRef.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = curriculumRef,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val desc = category.description
                if (!desc.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${category.itemsCount}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Emerald700
                )
                Text(
                    text = "Soal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuestionCategoryDetailContent(
    category: QuestionBankCategory,
    onNavigateToAutoGenerate: () -> Unit,
    onNavigateToManualCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Emerald100,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "Fase ${category.gradeLevel} • Kurikulum Merdeka",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!category.curriculumRef.isNullOrBlank()) {
                Text(
                    text = "Referensi CP: ${category.curriculumRef}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Distribusi Tingkat Kesulitan Soal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DifficultyBadge(title = "Mudah (C1-C2)", count = (category.itemsCount * 0.3).toInt(), color = Emerald700)
                        DifficultyBadge(title = "Sedang (C3-C4)", count = (category.itemsCount * 0.5).toInt(), color = Gold700)
                        DifficultyBadge(title = "Sukar (C5-C6 / HOTS)", count = (category.itemsCount * 0.2).toInt().coerceAtLeast(1), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAutoGenerate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Paket CBT")
                }
                OutlinedButton(
                    onClick = onNavigateToManualCreate,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Input Soal Baru")
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(title: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count Soal", fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.bodyLarge)
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyQuestionBankPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Quiz, contentDescription = null, tint = Emerald700, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Pilih Kategori Bank Soal", fontWeight = FontWeight.Bold)
            Text("Pilih kategori di samping untuk melihat butir soal dan komposisi kesulitan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
