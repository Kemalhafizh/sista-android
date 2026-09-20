/**
 * FASE 67: Overhauled Percakapan & Konsultasi Ortu ↔ Guru Screen.
 * Modern messaging portal: Slate50 off-white canvas, flat 0dp cards, 0.5dp hairline borders,
 * WCAG 2.2 AA typography, Emerald600 accents, and tactile haptic interactions.
 */
package com.sultanagung1.sista.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.ConversationItem

@Composable
fun ConversationListScreen(
    viewModel: ChatViewModel,
    onNavigateToChatRoom: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showNewConsultationDialog by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Wali Kelas", "Guru BK", "Tahfidz & PAI", "Tata Usaha")
    val borderColor = if (isDark) Slate800 else Slate200
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    val filteredConversations = remember(uiState.conversations, searchQuery, selectedCategory) {
        uiState.conversations.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.recipientName.contains(searchQuery, ignoreCase = true) ||
                    item.recipientRole.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "Wali Kelas" -> item.recipientRole.contains("Wali Kelas", ignoreCase = true)
                "Guru BK" -> item.recipientRole.contains("BK", ignoreCase = true) || item.recipientRole.contains("Konseling", ignoreCase = true)
                "Tahfidz & PAI" -> item.recipientRole.contains("Tahfidz", ignoreCase = true) || item.recipientRole.contains("Tahsin", ignoreCase = true) || item.recipientRole.contains("Arab", ignoreCase = true)
                "Tata Usaha" -> item.recipientRole.contains("Tata Usaha", ignoreCase = true) || item.recipientRole.contains("Keuangan", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pesan & Konsultasi Ortu ↔ Guru",
                subtitle = "SMA Islam Sultan Agung 1 Semarang",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptics.tapMedium()
                    showNewConsultationDialog = true
                },
                containerColor = Emerald600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                icon = { Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("Konsultasi Baru", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Search Bar & Category Filter Pills
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Cari Wali Kelas, Guru BK, Pembina Tahfidz...",
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
                                if (searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
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
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
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
                                            selectedCategory = cat
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else if (isDark) Slate300 else Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Count Header
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
                            text = "Daftar Percakapan Terbuka",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Slate100 else Slate900
                        )
                        Text(
                            text = "${filteredConversations.size} Kontak",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Slate400 else Slate500
                        )
                    }
                }

                // 3. Conversation Items or Empty State
                if (filteredConversations.isEmpty()) {
                    item {
                        SulaoneEmptyState(
                            icon = Icons.Default.ChatBubbleOutline,
                            title = "Belum Ada Percakapan",
                            description = "Percakapan dengan dewan guru atau konselor BK akan muncul di sini. Klik 'Konsultasi Baru' untuk memulai."
                        )
                    }
                } else {
                    items(filteredConversations, key = { it.id }) { conv ->
                        ConversationCard(
                            item = conv,
                            isDark = isDark,
                            borderColor = borderColor,
                            cardBg = cardBg,
                            onClick = {
                                haptics.tapLight()
                                viewModel.openConversation(conv.id)
                                onNavigateToChatRoom(conv.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewConsultationDialog) {
        NewConsultationModal(
            isDark = isDark,
            borderColor = borderColor,
            cardBg = cardBg,
            onDismiss = { showNewConsultationDialog = false },
            onStartConsultation = { teacherName, role, topic, initialMessage ->
                haptics.success()
                viewModel.sendMessage(initialMessage)
                showNewConsultationDialog = false
                onNavigateToChatRoom("conv1")
            }
        )
    }
}

@Composable
private fun ConversationCard(
    item: ConversationItem,
    isDark: Boolean,
    borderColor: Color,
    cardBg: Color,
    onClick: () -> Unit
) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Online indicator
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.recipientName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                        color = Emerald700,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (item.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Emerald600)
                            .border(2.dp, cardBg, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.recipientName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Slate100 else Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.lastMessageTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Slate400 else Slate500,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.recipientRole,
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald600,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Slate400 else Slate600,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Emerald600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NewConsultationModal(
    isDark: Boolean,
    borderColor: Color,
    cardBg: Color,
    onDismiss: () -> Unit,
    onStartConsultation: (String, String, String, String) -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    val teacherList = listOf(
        Pair("Ustadz Drs. H. Bambang Suherman", "Wali Kelas XII MIPA 1"),
        Pair("Ustadzah Fatimah, S.Psi", "Guru Bimbingan Konseling (BK)"),
        Pair("Ustadz Muhammad Luthfi, Lc", "Pembina Tahsin & Keislaman"),
        Pair("Ibu Sri Wahyuni, S.Pd", "Guru Mata Pelajaran Matematika"),
        Pair("Bpk. Joko Susilo, S.E.", "Tata Usaha & Keuangan SPP")
    )

    val topicList = listOf(
        "Perkembangan Akademik & Rapor",
        "Izin Sakit / Kehadiran Gerbang",
        "Karakter & Kedisiplinan Siswa",
        "Setoran Tahfidz Al-Qur'an",
        "Administrasi SPP & Beasiswa"
    )

    var selectedTeacher by remember { mutableStateOf(teacherList[0]) }
    var selectedTopic by remember { mutableStateOf(topicList[0]) }
    var messageText by remember { mutableStateOf("Assalamu'alaikum Warahmatullahi Wabarakatuh Ustadz, mohon izin berkonsultasi mengenai ananda.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = cardBg,
        title = {
            Text(
                text = "💬 Konsultasi Baru Ortu ↔ Guru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Slate100 else Slate900
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih Dewan Guru / Pendidik:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Slate300 else Slate700
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    teacherList.take(3).forEach { teacher ->
                        val isSelected = selectedTeacher == teacher
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50
                            } else {
                                if (isDark) Slate850 else Slate100
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSelected) Emerald600 else borderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptics.tapLight()
                                    selectedTeacher = teacher
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = teacher.first,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Slate100 else Slate900
                                )
                                Text(
                                    text = teacher.second,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Emerald600 else if (isDark) Slate400 else Slate500
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Topik Konsultasi:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Slate300 else Slate700
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(topicList) { topic ->
                        val isSelected = selectedTopic == topic
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Emerald600 else if (isDark) Slate850 else Slate100)
                                .border(0.5.dp, if (isSelected) Emerald600 else borderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptics.tapLight()
                                    selectedTopic = topic
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = topic,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else if (isDark) Slate300 else Slate700
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Pesan Pembuka", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald600,
                        unfocusedBorderColor = borderColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onStartConsultation(selectedTeacher.first, selectedTeacher.second, selectedTopic, messageText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Mulai Chat", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = if (isDark) Slate400 else Slate500)
            }
        }
    )
}
