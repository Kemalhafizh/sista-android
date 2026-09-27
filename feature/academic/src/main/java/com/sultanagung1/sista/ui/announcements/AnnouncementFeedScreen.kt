/**
 * FASE 67: Overhauled Pusat Informasi & Pengumuman Screen.
 * Modern institutional feed: Slate50 off-white canvas, flat 0dp cards, 0.5dp hairline borders,
 * WCAG 2.2 AA typography, Emerald600 accents, and tactile haptic interactions.
 */
package com.sultanagung1.sista.ui.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.AnnouncementItem

@Composable
fun AnnouncementFeedScreen(
    viewModel: AnnouncementViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    val categories = listOf("Semua", "Darurat", "Akademik", "Ibadah", "Kesiswaan")
    val borderColor = if (isDark) Slate800 else Slate200
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    val filteredList = remember(uiState.announcements, uiState.searchQuery, uiState.selectedCategory) {
        var list = uiState.announcements
        if (uiState.selectedCategory != "Semua") {
            list = list.filter { it.category.equals(uiState.selectedCategory, ignoreCase = true) }
        }
        if (uiState.searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(uiState.searchQuery, ignoreCase = true) ||
                it.summary.contains(uiState.searchQuery, ignoreCase = true) ||
                it.author.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
        list
    }

    // FASE 76.2: the list scrolls under a see-through top bar; the hairline
    // appears once content is actually passing beneath it.
    val listState = rememberLazyListState()
    val listScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pusat Informasi & Pengumuman",
                subtitle = "SMA Islam Sultan Agung 1 Semarang",
                onNavigateBack = onNavigateBack,
                translucent = true,
                showDivider = listScrolled
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Live WebSocket Alert Banner
                if (uiState.liveAlertBanner != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Emerald950 else Gold50),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Gold400)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Gold600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = uiState.liveAlertBanner ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Gold300 else Gold900,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        haptics.tapLight()
                                        viewModel.clearLiveBanner()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup",
                                        tint = if (isDark) Gold300 else Gold800,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Search Field & Category Chips
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Cari surat edaran, agenda, berita...",
                                    fontSize = 13.sp,
                                    color = if (isDark) Slate500 else Slate400
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (isDark) Slate400 else Slate500
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.updateSearchQuery("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Hapus",
                                            tint = if (isDark) Slate400 else Slate500
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = borderColor,
                                focusedContainerColor = cardBg,
                                unfocusedContainerColor = cardBg
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Pills
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                val isSelected = uiState.selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Emerald600 else cardBg)
                                        .border(
                                            width = 0.5.dp,
                                            color = if (isSelected) Emerald600 else borderColor,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .springPressable {
                                            haptics.tapLight()
                                            viewModel.selectCategory(category)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else if (isDark) Slate300 else Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Count Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Surat & Informasi Resmi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Slate100 else Slate900
                        )
                        Text(
                            text = if (filteredList.isEmpty() && uiState.isLoading) "Memuat…" else "${filteredList.size} Pengumuman",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Slate400 else Slate500
                        )
                    }
                }

                // 4. Feed Items, Loading, Error or Empty State.
                // "Tidak Ada Pengumuman" used to show while still loading and
                // after a failed request — the error was never displayed.
                uiState.errorMessage?.let { message ->
                    item {
                        SulaoneErrorBanner(
                            message = message,
                            onRetry = { viewModel.loadAnnouncements(uiState.selectedCategory) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                if (filteredList.isEmpty() && uiState.isLoading) {
                    item {
                        SulaoneTieredLoading(isLoading = true, modifier = Modifier.padding(horizontal = 16.dp)) {
                            AnnouncementSkeleton(borderColor = borderColor)
                        }
                    }
                } else if (filteredList.isEmpty() && uiState.errorMessage != null) {
                    // The banner above already explains; no false "no announcements".
                } else if (filteredList.isEmpty()) {
                    item {
                        SulaoneEmptyState(
                            icon = Icons.Default.Campaign,
                            title = "Tidak Ada Pengumuman",
                            description = "Belum ada edaran atau berita resmi untuk pencarian atau kategori ini."
                        )
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        AnnouncementItemCard(
                            item = item,
                            isDark = isDark,
                            borderColor = borderColor,
                            cardBg = cardBg,
                            onClick = {
                                haptics.tapLight()
                                viewModel.loadAnnouncementDetail(item.id)
                                onNavigateToDetail(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementItemCard(
    item: AnnouncementItem,
    isDark: Boolean,
    borderColor: Color,
    cardBg: Color,
    onClick: () -> Unit
) {
    val isEmergency = item.priority == "emergency"
    val isImportant = item.priority == "important"

    val badgeContainer = when {
        isEmergency -> if (isDark) AccentRose.copy(alpha = 0.25f) else AccentRose.copy(alpha = 0.12f)
        isImportant -> if (isDark) Gold900.copy(alpha = 0.4f) else Gold100
        else -> if (isDark) Emerald900.copy(alpha = 0.3f) else Emerald100
    }

    val badgeContent = when {
        isEmergency -> AccentRose
        isImportant -> if (isDark) Gold300 else Gold800
        else -> if (isDark) Emerald300 else Emerald800
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .springPressable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Priority/Category Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SulaoneBadge(
                        text = item.category.uppercase(),
                        containerColor = badgeContainer,
                        contentColor = badgeContent
                    )
                    if (isEmergency) {
                        Spacer(modifier = Modifier.width(6.dp))
                        LiveStatusChip("PENTING & MENDESAK", color = AccentRose)
                    }
                }

                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Slate400 else Slate500,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Slate100 else Slate900,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Slate400 else Slate600,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = borderColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Author & Read Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isDark) Slate500 else Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Slate400 else Slate500,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                ) {
                    Text(
                        text = "Baca Surat Edaran",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald600,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/** Mirrors an announcement card: category chip, title, two body lines, meta. */
@Composable
private fun AnnouncementSkeleton(borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(modifier = Modifier.width(72.dp).height(18.dp), shape = RoundedCornerShape(9.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(12.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
                SkeletonBox(modifier = Modifier.width(120.dp).height(10.dp))
            }
        }
    }
}
