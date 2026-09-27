package com.sultanagung1.sista.ui.elearning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sultanagung1.sista.data.model.ElearningAssignmentItem
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.ElearningMaterialItem

@Composable
fun ElearningClassDetailScreen(
    classId: Long,
    viewModel: ElearningViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSubmitAssignment: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Materi Belajar", "Tugas & Evaluasi")

    LaunchedEffect(classId) {
        viewModel.loadClassDetails(classId)
    }

    val currentClass = uiState.selectedClass ?: uiState.classes.find { it.id == classId }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald700
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> MaterialsTabContent(
                    materials = uiState.materials,
                    isLoading = uiState.isLoading
                )
                1 -> AssignmentsTabContent(
                    assignments = uiState.assignments,
                    isLoading = uiState.isLoading,
                    onNavigateToSubmit = onNavigateToSubmitAssignment
                )
            }
        }
    }
}

@Composable
fun MaterialsTabContent(
    materials: List<ElearningMaterialItem>,
    isLoading: Boolean
) {
    if (isLoading && materials.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Emerald600)
        }
    } else if (materials.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Belum ada modul materi ajar yang diunggah untuk kelas ini.",
                color = Slate400,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(materials) { item ->
                MaterialCardItem(item = item)
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MaterialCardItem(item: ElearningMaterialItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = when (item.type?.lowercase()) {
                        "video" -> AccentRose.copy(alpha = 0.15f)
                        "pdf" -> AccentAmber.copy(alpha = 0.15f)
                        else -> Emerald50
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (item.type?.lowercase()) {
                                "video" -> Icons.Default.PlayCircle
                                "pdf" -> Icons.Default.PictureAsPdf
                                else -> Icons.Default.MenuBook
                            },
                            contentDescription = null,
                            tint = when (item.type?.lowercase()) {
                                "video" -> AccentRose
                                "pdf" -> AccentAmber
                                else -> Emerald700
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Diunggah: ${item.createdAt ?: "Terbaru"}",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            if (!item.content.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.content,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = { /* Open file or link */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Emerald50,
                    contentColor = Emerald800
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buka / Unduh Materi", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AssignmentsTabContent(
    assignments: List<ElearningAssignmentItem>,
    isLoading: Boolean,
    onNavigateToSubmit: (Long) -> Unit
) {
    if (isLoading && assignments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Emerald600)
        }
    } else if (assignments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Tidak ada tugas atau proyek aktif di kelas ini.",
                color = Slate400,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assignments) { item ->
                AssignmentCardItem(
                    item = item,
                    onSubmitClick = { onNavigateToSubmit(item.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AssignmentCardItem(
    item: ElearningAssignmentItem,
    onSubmitClick: () -> Unit
) {
    val isSubmitted = item.isSubmitted || item.submission != null
    val score = item.submission?.score

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSubmitted) Emerald50 else Gold50
                ) {
                    Text(
                        text = if (isSubmitted) {
                            if (score != null) "Ternilai ($score/100)" else "Sudah Dikumpulkan"
                        } else "Belum Mengumpulkan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubmitted) Emerald700 else AccentAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Slate400
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.dueAt,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            if (item.submission?.feedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Catatan Guru:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Emerald800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            item.submission.feedback,
                            fontSize = 12.sp,
                            color = Emerald900
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isSubmitted) {
                OutlinedButton(
                    onClick = onSubmitClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Emerald700
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Perbarui Pengumpulan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = onSubmitClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald700
                    )
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kumpulkan Tugas Sekarang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
