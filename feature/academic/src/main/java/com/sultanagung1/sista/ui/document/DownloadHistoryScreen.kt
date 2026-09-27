package com.sultanagung1.sista.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.document.DownloadManager
import com.sultanagung1.sista.data.model.DownloadTaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadHistoryScreen(
    downloadManager: DownloadManager,
    onNavigateBack: () -> Unit,
    onOpenFile: (DownloadTaskItem) -> Unit = {}
) {
    val downloads by downloadManager.downloads.collectAsState()
    var selectedTab by remember { mutableStateOf("Semua") }

    val categories = listOf("Semua", "PDF Dokumen", "Modul KBM", "Kuitansi")

    val filteredDownloads = when (selectedTab) {
        "PDF Dokumen" -> downloads.filter { it.fileType == "PDF" }
        else -> downloads
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Manajer Unduhan File",
                subtitle = "Riwayat Rapor, Modul KBM & Kuitansi",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedTab == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedTab = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Downloads List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDownloads, key = { it.id }) { item ->
                    DownloadCardItem(
                        item = item,
                        onOpen = { onOpenFile(item) },
                        onDelete = { downloadManager.removeDownload(item.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DownloadCardItem(
    item: DownloadTaskItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (item.fileType == "PDF") Emerald100 else Gold100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = if (item.fileType == "PDF") Emerald800 else Gold800,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${item.sizeBytes / 1024} KB • ${item.downloadedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!item.isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Emerald700
                    )
                }
            }

            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Buka File",
                    tint = Emerald700
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Hapus",
                    tint = Slate400
                )
            }
        }
    }
}
