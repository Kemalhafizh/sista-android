package com.sultanagung1.sista.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

/**
 * Adaptive Chat Scaffold yang mengimplementasikan List-Detail Dual-Pane (FASE 55.1).
 * - Smartphone (Compact): Menampilkan ConversationListScreen biasa, klik berpindah ke ChatScreen.
 * - Tablet & Foldable (Medium & Expanded): Menampilkan ConversationListScreen di pane kiri
 *   dan ChatScreen aktif di pane kanan secara berdampingan.
 */
@Composable
fun AdaptiveChatScreen(
    viewModel: ChatViewModel,
    onNavigateToChatRoom: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val windowWidthClass = LocalWindowWidthSizeClass.current
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    // Auto-select percakapan pertama saat mode tablet jika belum ada yang dipilih
    LaunchedEffect(windowWidthClass, uiState.conversations) {
        if (windowWidthClass != WindowWidthSizeClass.Compact && selectedConversationId == null && uiState.conversations.isNotEmpty()) {
            selectedConversationId = uiState.conversations.first().id
        }
    }

    if (windowWidthClass == WindowWidthSizeClass.Compact) {
        // Mode Smartphone: Navigasi Single Pane
        ConversationListScreen(
            viewModel = viewModel,
            onNavigateToChatRoom = onNavigateToChatRoom,
            onNavigateBack = onNavigateBack
        )
    } else {
        // Mode Tablet & Foldable: Dual-Pane List-Detail Scaffold
        SulaoneListDetailPaneScaffold(
            selectedItem = selectedConversationId ?: "",
            onSelectItem = { selectedConversationId = it },
            listPaneWidth = 360.dp,
            listPane = {
                ConversationListScreen(
                    viewModel = viewModel,
                    onNavigateToChatRoom = { convId ->
                        selectedConversationId = convId
                    },
                    onNavigateBack = onNavigateBack
                )
            },
            detailPane = { convId, _ ->
                if (!convId.isNullOrBlank()) {
                    ChatScreen(
                        conversationId = convId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            selectedConversationId = null
                        }
                    )
                } else {
                    EmptyChatPlaceholder()
                }
            },
            emptyDetailPlaceholder = {
                EmptyChatPlaceholder()
            }
        )
    }
}

@Composable
private fun EmptyChatPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pilih Percakapan Konsultasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pilih salah satu kontak guru, wali kelas, atau pembimbing tahfidz di sebelah kiri untuk membuka ruang konsultasi interaktif.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 380.dp)
            )
        }
    }
}
