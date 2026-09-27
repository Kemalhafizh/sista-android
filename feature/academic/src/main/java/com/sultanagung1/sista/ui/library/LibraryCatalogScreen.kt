package com.sultanagung1.sista.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedElement

@Composable
fun LibraryCatalogScreen(
    viewModel: LibraryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()

    val categories = listOf("Semua", "Sains & Teknologi", "Keislaman & Tarbiyah", "Persiapan UTBK", "Sastra & Bahasa")
    val tabs = listOf("Katalog & Stok Buku", "Pinjaman Saya (${uiState.myLoans.size})")

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "E-Pustaka Pintar",
                subtitle = "Perpustakaan SMA Islam Sultan Agung 1",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptics.tapHeavy()
                    onNavigateToScanner()
                },
                containerColor = Emerald700,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text("Self-Checkout QR", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tabs.forEachIndexed { index, tabName ->
                    val isSelected = uiState.selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                if (isSelected) Emerald600 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            )
                            .springPressable {
                                haptics.tapLight()
                                viewModel.selectTab(index)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Pull-to-refresh used to call selectTab() — which reloads nothing —
            // and spin for a fixed 600 ms. It now reloads and spins exactly as
            // long as the real request runs.
            var refreshRequested by remember { mutableStateOf(false) }
            LaunchedEffect(uiState.isLoading) {
                if (!uiState.isLoading) refreshRequested = false
            }

            SulaonePullToRefreshBox(
                isRefreshing = refreshRequested && uiState.isLoading,
                onRefresh = {
                    refreshRequested = true
                    viewModel.refresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (uiState.selectedTab) {
                        0 -> {
                            // Search Bar
                            item {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Cari judul buku, penulis, atau ISBN...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald700) },
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald700,
                                    unfocusedBorderColor = Slate300
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Category Pills
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { cat ->
                                    val isSelected = uiState.selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Gold400 else Slate100)
                                            .springPressable {
                                                haptics.tapLight()
                                                viewModel.selectCategory(cat)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                            color = if (isSelected) Slate950 else Slate700
                                        )
                                    }
                                }
                            }
                        }

                        // FASE 76.5: loading / error / empty used to render nothing here.
                        if (uiState.books.isEmpty()) {
                            item {
                                when {
                                    uiState.isLoading -> SulaoneTieredLoading(isLoading = true) { BookListSkeleton() }
                                    uiState.errorMessage != null -> SulaoneErrorBanner(
                                        message = uiState.errorMessage ?: "Gagal memuat katalog.",
                                        onRetry = { viewModel.refresh() }
                                    )
                                    else -> SulaoneEmptyState(
                                        title = "Buku Tidak Ditemukan",
                                        description = "Tidak ada buku yang cocok dengan pencarian atau kategori ini.",
                                        icon = Icons.Default.SearchOff
                                    )
                                }
                            }
                        }

                        // Book Catalog Cards
                        items(uiState.books) { book ->
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Book Icon / Cover
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .sulaoneSharedElement(key = "book_cover_${book.id}")
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Emerald100),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Emerald800, modifier = Modifier.size(28.dp))
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = book.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            LiveStatusChip(
                                                text = if (book.isAvailable) "${book.availableCopies} Tersedia" else "Habis",
                                                color = if (book.isAvailable) Emerald700 else AccentRose
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Penulis: ${book.author} • ${book.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Slate100
                                        ) {
                                            Text(
                                                text = "📍 ${book.shelfLocation} (ISBN: ${book.isbn})",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = Slate700,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = book.synopsis,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Slate600,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // My Active Loans Tab — "no loans" is only claimed once loaded.
                        if (uiState.myLoans.isEmpty() && uiState.isLoading) {
                            item { SulaoneTieredLoading(isLoading = true) { BookListSkeleton(rows = 2) } }
                        } else if (uiState.myLoans.isEmpty() && uiState.errorMessage != null) {
                            item {
                                SulaoneErrorBanner(
                                    message = uiState.errorMessage ?: "Gagal memuat pinjaman.",
                                    onRetry = { viewModel.refresh() }
                                )
                            }
                        } else if (uiState.myLoans.isEmpty()) {
                            item {
                                SulaoneEmptyState(
                                    title = "Belum Ada Buku yang Dipinjam",
                                    description = "Gunakan tombol Self-Checkout QR untuk meminjam buku fisik di perpustakaan secara instan.",
                                    icon = Icons.Default.LibraryBooks
                                )
                            }
                        } else {
                            items(uiState.myLoans) { loan ->
                                val isCritical = loan.daysRemaining <= 1
                                ModernBentoCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = if (isCritical) AccentAmber.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                    borderColor = if (isCritical) AccentAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    glowColor = if (isCritical) GoldGlow else null
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isCritical) AccentAmber else Emerald700
                                            ) {
                                                Text(
                                                    text = if (isCritical) "⚠️ Sisa ${loan.daysRemaining} Hari (H-1)" else "Sisa ${loan.daysRemaining} Hari",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            Text(
                                                text = "Jatuh Tempo: ${loan.dueDate}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate600
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = loan.bookTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Dipinjam sejak: ${loan.borrowDate} • Bebas denda jika dikembalikan tepat waktu.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
            }
        }
    }
}

/** Mirrors a book card: cover on the left, title / author / meta lines. */
@Composable
private fun BookListSkeleton(rows: Int = 4) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(modifier = Modifier.width(56.dp).height(76.dp), shape = RoundedCornerShape(8.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(12.dp))
                    SkeletonBox(modifier = Modifier.width(90.dp).height(20.dp), shape = RoundedCornerShape(10.dp))
                }
            }
        }
    }
}
