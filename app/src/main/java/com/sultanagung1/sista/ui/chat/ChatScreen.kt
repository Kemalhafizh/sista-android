/**
 * FASE 67: Overhauled Chat Room & Konsultasi Ortu ↔ Guru Screen.
 * Modern messaging experience: Slate50 off-white canvas, flat 0dp bubbles, 0.5dp hairline borders,
 * WCAG 2.2 AA typography, Emerald600 accents, and tactile haptic interactions.
 */
package com.sultanagung1.sista.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    conversationId: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val conv = uiState.activeConversation
    val messages = uiState.messages
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    var textInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    val borderColor = if (isDark) Slate800 else Slate200
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    val quickReplies = listOf(
        "Wa'alaikumsalam Ustadz 🙏",
        "Baik Ustadz, terima kasih atas bimbingannya.",
        "Mohon izin ananda sakit hari ini, surat dokter terlampir.",
        "Bagaimana perkembangan hafalan tahfidz ananda?",
        "InsyaAllah kami dampingi belajar di rumah."
    )

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = cardBg,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptics.tapLight()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Slate800 else Slate100)
                            .sulaoneInteractiveTouchTarget(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (isDark) Slate200 else Slate700
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Contact Avatar with Online Dot
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (conv?.recipientName ?: "B").split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                                color = Emerald700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        if (conv?.isOnline == true) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Emerald600)
                                    .border(2.dp, cardBg, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conv?.recipientName ?: "—",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Slate100 else Slate900,
                            maxLines = 1
                        )
                        Text(
                            text = if (uiState.isRecipientTyping) "sedang mengetik..." else (conv?.recipientRole ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isRecipientTyping) Emerald600 else if (isDark) Slate400 else Slate500,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.tapLight()
                            val waUrl = "https://api.whatsapp.com/send?phone=6281234567890&text=Assalamu%27alaikum%20Ustadz"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Hubungi WhatsApp",
                            tint = Emerald600
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
        ) {
            // Typing Indicator banner
            AnimatedVisibility(visible = uiState.isRecipientTyping) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Emerald950 else Emerald50)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Ustadz sedang mengetik pesan balasan...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Emerald200 else Emerald800,
                        fontSize = 11.sp
                    )
                }
            }

            // Message History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Slate850 else Slate100,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
                        ) {
                            Text(
                                text = "🔒 Percakapan resmi terjaga & terpantau sistem SISTA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Slate400 else Slate500,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        isDark = isDark,
                        borderColor = borderColor,
                        cardBg = cardBg
                    )
                }
            }

            // Quick Reply Suggestions
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickReplies) { reply ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Emerald900.copy(alpha = 0.3f) else Emerald50)
                            .border(
                                0.5.dp,
                                if (isDark) Emerald700.copy(alpha = 0.5f) else Emerald200,
                                RoundedCornerShape(16.dp)
                            )
                            .springPressable {
                                haptics.tapLight()
                                viewModel.sendMessage(reply)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = reply,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Emerald200 else Emerald800
                        )
                    }
                }
            }

            // Bottom Message Input Row
            Surface(
                color = cardBg,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptics.tapLight()
                            showAttachmentSheet = true
                        },
                        modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Lampiran",
                            tint = if (isDark) Slate400 else Slate500
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ketik pesan konsultasi...",
                                fontSize = 13.sp,
                                color = if (isDark) Slate500 else Slate400
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = borderColor,
                            focusedContainerColor = if (isDark) Slate900 else Slate50,
                            unfocusedContainerColor = if (isDark) Slate900 else Slate50
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                haptics.tapHeavy()
                                viewModel.sendMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Emerald600)
                            .sulaoneInteractiveTouchTarget(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAttachmentSheet) {
        AlertDialog(
            onDismissRequest = { showAttachmentSheet = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = cardBg,
            title = {
                Text(
                    "Kirim Lampiran Resmi",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Slate100 else Slate900
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Emerald900.copy(alpha = 0.3f) else Emerald50,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDark) Emerald700 else Emerald200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptics.success()
                                viewModel.sendMessage("[LAMPIRAN DOKUMEN: Surat_Keterangan_Dokter.pdf]")
                                showAttachmentSheet = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Emerald600)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Surat Keterangan Dokter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDark) Slate100 else Slate900
                                )
                                Text(
                                    "Upload bukti izin sakit resmi",
                                    fontSize = 11.sp,
                                    color = if (isDark) Slate400 else Slate500
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) AccentBlue.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptics.success()
                                viewModel.sendMessage("[LAMPIRAN DOKUMEN: Piagam_Penghargaan_Lomba.pdf]")
                                showAttachmentSheet = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Piagam / Berkas Prestasi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDark) Slate100 else Slate900
                                )
                                Text(
                                    "Upload bukti sertifikat juara",
                                    fontSize = 11.sp,
                                    color = if (isDark) Slate400 else Slate500
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentSheet = false }) {
                    Text("Tutup", color = if (isDark) Slate400 else Slate500)
                }
            }
        )
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isDark: Boolean,
    borderColor: Color,
    cardBg: Color
) {
    val isMe = message.isMe
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isMe) Emerald600 else cardBg
    val textColor = if (isMe) Color.White else if (isDark) Slate100 else Slate900
    val timeColor = if (isMe) Emerald100 else if (isDark) Slate400 else Slate500

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .then(
                    if (!isMe) Modifier.border(0.5.dp, borderColor, bubbleShape) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald600,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                if (message.text.startsWith("[LAMPIRAN")) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMe) Emerald700 else (if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isMe) Gold300 else Emerald700,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text.removeSurrounding("[", "]"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White else Emerald800
                            )
                        }
                    }
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = timeColor,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.status) {
                                "read" -> Icons.Default.DoneAll
                                "delivered" -> Icons.Default.DoneAll
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = if (message.status == "read") Gold300 else Emerald200,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
