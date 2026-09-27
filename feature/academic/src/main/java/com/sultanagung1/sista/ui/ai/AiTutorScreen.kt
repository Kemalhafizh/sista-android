package com.sultanagung1.sista.ui.ai

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.AiChatMessage

/**
 * Sultan AI Tutor chat (FASE 47), reworked in FASE 76.7 against the 2026 chat
 * checklist:
 * - Capability transparency up front: what it does, what it doesn't, and that
 *   the current server replies come from fixed rules (AiPersonalizedTutorService
 *   picks one of three templates), not a generative model. The old header said
 *   "Online 24/7", which the app cannot know.
 * - No confidence indicator: the only number the server returns
 *   (comprehension_score) is derived from the student's word count, so showing
 *   it as confidence would be invented.
 * - Recovery: a failed question stays in the thread, marked, and "Coba Lagi"
 *   resends it. The old retry only hid the error.
 * - Composer docked above the keyboard (the app is edge-to-edge, so without
 *   imePadding the keyboard covered the text field), a 48dp send button, one
 *   request at a time.
 * - Long replies split into short bubbles (AiReplyFormatting), and the thread
 *   follows the newest message.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiTutorScreen(
    viewModel: AiViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by rememberSaveable { mutableStateOf("") }
    val haptics = rememberHapticFeedbackHelper()
    val listState = rememberLazyListState()

    val quickPrompts = if (uiState.suggestions.isNotEmpty()) {
        uiState.suggestions
    } else {
        listOf(
            "📐 Bantu soal Turunan Matematika",
            "🔬 Jelaskan Hukum Termodinamika II",
            "📖 Ringkas Sejarah Perang Badar",
            "🧪 Cara seimbangkan Reaksi Redoks",
            "📝 Perbaiki struktur paragraf esai"
        )
    }

    val canSend = inputText.isNotBlank() && !uiState.isLoading
    val send: () -> Unit = {
        if (canSend) {
            haptics.tapHeavy()
            viewModel.sendMessage(inputText.trim())
            inputText = ""
        }
    }

    // Item count mirrors the LazyColumn below: capability card, messages,
    // typing indicator, error banner, bottom spacer.
    val lastIndex = 1 + uiState.messages.size +
        (if (uiState.isLoading) 1 else 0) +
        (if (uiState.errorMessage != null) 1 else 0)
    LaunchedEffect(lastIndex) {
        listState.animateScrollToItem(lastIndex)
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Sultan AI Tutor",
                subtitle = "Belajar dengan pertanyaan balik (Socratic)",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "capabilities") {
                    Spacer(modifier = Modifier.height(10.dp))
                    TutorCapabilityCard()
                }

                items(uiState.messages) { msg ->
                    TutorMessage(msg = msg, failed = msg.id == uiState.failedMessageId)
                }

                if (uiState.isLoading) {
                    item(key = "typing") { TypingIndicator() }
                }

                uiState.errorMessage?.let { message ->
                    item(key = "error") {
                        SulaoneErrorBanner(
                            message = message,
                            onRetry = { viewModel.retryLastMessage() },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Quick prompts, disabled while a reply is pending.
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .alpha(if (uiState.isLoading) 0.5f else 1f)
                            .springPressable {
                                if (!uiState.isLoading) {
                                    haptics.tapLight()
                                    viewModel.sendMessage(prompt)
                                }
                            }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Composer, docked to the bottom and lifted above the keyboard by imePadding().
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Tanyakan konsep pelajaran...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        singleLine = false,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald700,
                            unfocusedBorderColor = Slate300
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { send() })
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    FilledIconButton(
                        onClick = send,
                        enabled = canSend,
                        modifier = Modifier.size(52.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Emerald700,
                            contentColor = Color.White,
                            disabledContainerColor = Slate300,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim pertanyaan",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/** What the tutor does and doesn't do, stated before the first question. */
@Composable
private fun TutorCapabilityCard() {
    ModernBentoCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Emerald50,
        borderColor = Emerald200,
        glowColor = EmeraldGlow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Emerald700),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Gold400,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Yang bisa & belum bisa",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Emerald900
                )
            }
            CapabilityLine(
                can = true,
                text = "Memandu dengan pertanyaan balik sampai kamu menemukan jawabannya sendiri"
            )
            CapabilityLine(can = false, text = "Tidak memberi jawaban akhir atau kunci soal")
            CapabilityLine(can = false, text = "Belum bisa membaca foto soal atau menghitung rumus")
            Text(
                text = TUTOR_ENGINE_DISCLOSURE,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Emerald800
            )
        }
    }
}

/**
 * Stated because it is true of the server today (AiPersonalizedTutorService
 * chooses among three fixed replies). Update this line when a real model is
 * wired in, not before.
 */
internal const val TUTOR_ENGINE_DISCLOSURE =
    "Versi saat ini membalas dengan pola tetap dari server sekolah, belum model AI generatif, jadi balasannya bisa kurang nyambung. Kalau ragu, tanyakan ke guru mapel."

@Composable
private fun CapabilityLine(can: Boolean, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (can) Icons.Default.CheckCircle else Icons.Default.Block,
            contentDescription = if (can) "Bisa" else "Belum bisa",
            tint = if (can) Emerald700 else Slate500,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Emerald900
        )
    }
}

@Composable
private fun TutorMessage(msg: AiChatMessage, failed: Boolean) {
    val isUser = msg.sender == "USER"
    if (isUser) {
        ChatBubble(text = msg.message, isUser = true, showAvatar = true, failed = failed)
    } else {
        val parts = remember(msg.message) { AiReplyFormatting.splitIntoBubbles(msg.message) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            parts.forEachIndexed { index, part ->
                ChatBubble(text = part, isUser = false, showAvatar = index == 0)
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isUser: Boolean,
    showAvatar: Boolean,
    failed: Boolean = false
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
    )
    val styled = remember(text) {
        val parsed = AiReplyFormatting.parseBold(text)
        buildAnnotatedString {
            append(parsed.text)
            parsed.boldRanges.forEach { range ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), range.first, range.last + 1)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                AvatarSlot(visible = showAvatar, icon = Icons.Default.AutoAwesome, background = Emerald700, tint = Gold400, description = "Tutor")
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clip(bubbleShape)
                    .background(if (isUser) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        when {
                            failed -> AccentRose
                            isUser -> Emerald600
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        },
                        bubbleShape
                    )
                    .padding(14.dp)
            ) {
                // Selectable so a student can copy a hint into their notes.
                SelectionContainer {
                    Text(
                        text = styled,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(10.dp))
                AvatarSlot(visible = showAvatar, icon = Icons.Default.Person, background = Gold400, tint = Slate950, description = "Kamu")
            }
        }
        if (failed) {
            Row(
                modifier = Modifier.padding(top = 4.dp, end = 46.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Gagal terkirim",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentRose
                )
            }
        }
    }
}

/** Keeps follow-up bubbles aligned with the first one even when the avatar is hidden. */
@Composable
private fun AvatarSlot(visible: Boolean, icon: ImageVector, background: Color, tint: Color, description: String) {
    if (visible) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
        }
    } else {
        Spacer(modifier = Modifier.size(36.dp))
    }
}

/** Three pulsing dots, the familiar "the other side is writing" signal. */
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "tutorTyping")
    Row(
        modifier = Modifier
            .padding(start = 46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { contentDescription = "Sultan AI Tutor sedang menyusun balasan" },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val dotAlpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 450),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 150)
                ),
                label = "typingDot$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(Emerald700)
            )
        }
    }
}
