/**
 * FASE 67 / FASE 72.3: Chat Room & Konsultasi Ortu ↔ Guru Screen.
 * Modern messaging experience: Slate50 off-white canvas, flat 0dp bubbles, 0.5dp hairline borders,
 * WCAG 2.2 AA typography, Emerald600 accents, and tactile haptic interactions.
 *
 * Real-time additions (72.3): typing indicator now actually reflects a live
 * WebSocket whisper (see ChatViewModel.sendTypingStatus / ReverbWebSocketManager's
 * "client-typing" handling) instead of only ever displaying a receive-side
 * boolean nothing produced; attachments are a real image/PDF picker uploaded
 * via multipart and rendered from the server's real attachment_url — the old
 * "attachment sheet" sent a literal text string like
 * "[LAMPIRAN DOKUMEN: Surat_Keterangan_Dokter.pdf]" for one of two hardcoded
 * fake documents regardless of what (if anything) existed on device.
 */
package com.sultanagung1.sista.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.ChatAttachment
import com.sultanagung1.sista.data.model.ChatMessage
import kotlinx.coroutines.delay

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
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    var textInput by remember { mutableStateOf("") }
    var pendingAttachment by remember { mutableStateOf<PickedAttachment?>(null) }

    val borderColor = if (isDark) Slate800 else Slate200
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    val quickReplies = listOf(
        "Wa'alaikumsalam Ustadz 🙏",
        "Baik Ustadz, terima kasih atas bimbingannya.",
        "Mohon izin ananda sakit hari ini, surat dokter terlampir.",
        "Bagaimana perkembangan hafalan tahfidz ananda?",
        "InsyaAllah kami dampingi belajar di rumah."
    )

    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) {
            pendingAttachment = PickedAttachment(uri, bytes, mimeType)
        }
    }

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // FASE 72.3: broadcast a real typing whisper — restarts a 2s "stopped
    // typing" countdown on every keystroke instead of only ever listening.
    LaunchedEffect(textInput) {
        if (textInput.isNotBlank()) {
            viewModel.sendTypingStatus(true)
            delay(2000)
            viewModel.sendTypingStatus(false)
        } else {
            viewModel.sendTypingStatus(false)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.sendTypingStatus(false) }
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
                        if (uiState.isRecipientTyping) {
                            TypingDots(color = Emerald600)
                        } else {
                            Text(
                                text = conv?.recipientRole ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Slate400 else Slate500,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
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

                if (uiState.isRecipientTyping) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = cardBg,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                    TypingDots(color = if (isDark) Slate400 else Slate500)
                                }
                            }
                        }
                    }
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

            // Pending attachment preview (before send)
            pendingAttachment?.let { picked ->
                Surface(
                    color = cardBg,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (picked.mimeType.startsWith("image/")) {
                            AsyncImage(
                                model = picked.uri,
                                contentDescription = "Pratinjau lampiran",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Slate800 else Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AccentRose)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (picked.mimeType.startsWith("image/")) "Gambar siap dikirim" else "Dokumen PDF siap dikirim",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Slate300 else Slate700,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { pendingAttachment = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Batal", tint = AccentRose)
                        }
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
                            attachmentPicker.launch(arrayOf("image/*", "application/pdf"))
                        },
                        modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Lampiran",
                            tint = if (pendingAttachment != null) Emerald600 else if (isDark) Slate400 else Slate500
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
                            if (textInput.isNotBlank() || pendingAttachment != null) {
                                haptics.tapHeavy()
                                val attachment = pendingAttachment?.let {
                                    ChatAttachment(
                                        bytes = it.bytes,
                                        fileName = if (it.mimeType == "application/pdf") "lampiran_${System.currentTimeMillis()}.pdf" else "lampiran_${System.currentTimeMillis()}.jpg",
                                        mimeType = it.mimeType
                                    )
                                }
                                viewModel.sendMessage(textInput.trim(), attachment)
                                textInput = ""
                                pendingAttachment = null
                            }
                        },
                        enabled = textInput.isNotBlank() || pendingAttachment != null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (textInput.isNotBlank() || pendingAttachment != null) Emerald600 else Slate300)
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
}

private data class PickedAttachment(val uri: Uri, val bytes: ByteArray, val mimeType: String)

/** FASE 72.3: animated three-dot "typing" indicator. */
@Composable
private fun TypingDots(color: Color) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = index * 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.5.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
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
    val context = LocalContext.current

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

                // FASE 72.3: real attachment preview from the server's real
                // attachment_url/attachment_type — previously a fake bubble
                // was drawn whenever the message text happened to start with
                // the literal string "[LAMPIRAN", which was itself only ever
                // produced by the old fake attachment sheet.
                if (!message.attachmentUrl.isNullOrBlank()) {
                    if (message.attachmentType == "pdf") {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMe) Emerald700 else (if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50),
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(message.attachmentUrl)))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = if (isMe) Gold300 else Emerald700,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dokumen PDF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White else Emerald800
                                )
                            }
                        }
                    } else {
                        AsyncImage(
                            model = message.attachmentUrl,
                            contentDescription = "Lampiran gambar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .widthIn(max = 220.dp)
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(message.attachmentUrl)))
                                }
                        )
                    }
                }

                if (message.text.isNotBlank()) {
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
                            // Only "sent"/"read" are real (see ChatMessage.status) —
                            // no "delivered" tick exists.
                            imageVector = if (message.status == "read") Icons.Default.DoneAll else Icons.Default.Check,
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
