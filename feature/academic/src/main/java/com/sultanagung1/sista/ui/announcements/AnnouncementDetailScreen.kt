package com.sultanagung1.sista.ui.announcements

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import coil.compose.AsyncImage
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.AnnouncementItem
import java.time.LocalDate
import java.time.ZoneId

/** One announcement in full. Opening it is recorded as read on the server. */
@Composable
fun AnnouncementDetailScreen(
    viewModel: AnnouncementDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    AnnouncementDetailContent(
        state = state,
        today = remember(state.announcement) { LocalDate.now(ZoneId.of("Asia/Jakarta")) },
        onRetry = viewModel::load,
        onAcknowledge = viewModel::acknowledge,
        onOpenAttachment = { url ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: ActivityNotFoundException) {
                // No app on this phone opens the file; the link stays on screen.
            }
        },
        onShare = { item ->
            val text = listOfNotNull(item.title, item.summary.takeIf { it.isNotBlank() }).joinToString("\n\n")
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Bagikan pengumuman"))
        },
        onNavigateBack = onNavigateBack,
    )
}

/** The announcement detail without a ViewModel, for previews and screenshots. */
@Composable
fun AnnouncementDetailContent(
    state: AnnouncementDetailUiState,
    today: LocalDate,
    onRetry: () -> Unit,
    onAcknowledge: () -> Unit,
    onOpenAttachment: (String) -> Unit,
    onShare: (AnnouncementItem) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val item = state.announcement
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SistaTopBar(
                    title = "Pengumuman",
                    scrollBehavior = scrollBehavior,
                    onBack = onNavigateBack,
                    actions = {
                        if (item != null) {
                            IconButton(onClick = { onShare(item) }) { Icon(Icons.Outlined.Share, contentDescription = "Bagikan") }
                        }
                    },
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("announcement_detail_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                when {
                    item == null && state.errorMessage != null -> item(key = "error") {
                        ErrorState(title = "Pengumuman belum bisa dibuka", body = state.errorMessage, onRetry = onRetry)
                    }
                    item == null -> item(key = "loading") { SkeletonList(rows = 3) }
                    else -> {
                        item(key = "header") { Header(item, today) }
                        absoluteUrl(item.coverImageUrl)?.let { url ->
                            item(key = "cover") {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Gambar pengumuman",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(SistaTheme.shapes.medium),
                                )
                            }
                        }
                        item(key = "body") {
                            Text(
                                remember(item.content) { AnnotatedString.fromHtml(item.content) },
                                style = SistaTheme.typography.bodyLarge,
                            )
                        }
                        absoluteUrl(item.attachmentUrl)?.let { url ->
                            item(key = "attachment") { Attachment(url, onOpen = { onOpenAttachment(url) }) }
                        }
                        if (item.requireAcknowledgement) {
                            item(key = "ack") { Acknowledgement(item, state.acknowledging, state.acknowledgeError, onAcknowledge) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(item: AnnouncementItem, today: LocalDate) {
    val category = AnnouncementCategory.of(item.category)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            if (isUrgent(item)) StatusPill(text = "Penting", tone = StatusTone.Danger)
            StatusPill(text = category.label, tone = category.tone())
            item.audience?.takeIf { it.isNotBlank() }?.let { StatusPill(text = "Untuk: $it", tone = StatusTone.Neutral) }
        }
        Text(item.title, style = SistaTheme.typography.headlineSmall)
        Text(
            listOfNotNull(item.author.takeIf { it.isNotBlank() }, publishedLabel(item, today).takeIf { it.isNotBlank() }).joinToString(" · "),
            style = SistaTheme.typography.bodyMedium,
            color = SistaTheme.colors.onSurfaceVariant,
        )
        expiryLabel(item)?.let {
            Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
        }
        HorizontalDivider(color = SistaTheme.colors.outlineVariant, modifier = Modifier.padding(top = Spacing.sm))
    }
}

@Composable
private fun Attachment(url: String, onOpen: () -> Unit) {
    val name = Uri.decode(url.substringAfterLast('/').substringBefore('?')).ifBlank { "Lampiran" }
    SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconBadge(icon = Icons.Outlined.AttachFile, tone = StatusTone.Info)
            Column(Modifier.weight(1f)) {
                Text("Lampiran", style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
                Text(name, style = SistaTheme.typography.bodyLarge, maxLines = 2)
            }
            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "Buka lampiran", tint = SistaTheme.colors.primary)
        }
    }
}

@Composable
private fun Acknowledgement(item: AnnouncementItem, sending: Boolean, error: String?, onAcknowledge: () -> Unit) {
    val acknowledgedAt = parseTime(item.acknowledgedAt)
    if (acknowledgedAt != null) {
        InlineBanner(
            message = "Anda sudah mengonfirmasi telah membaca pengumuman ini (${acknowledgedAt.dayOfMonth}/${acknowledgedAt.monthValue}/${acknowledgedAt.year}).",
            tone = StatusTone.Success,
        )
        return
    }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                IconBadge(icon = Icons.Outlined.TaskAlt, tone = StatusTone.Warning)
                Text(
                    "Sekolah meminta Anda mengonfirmasi bahwa pengumuman ini sudah dibaca dan dipahami.",
                    style = SistaTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
            error?.let { Text(it, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.error) }
            SistaButton(text = "Saya sudah membaca", onClick = onAcknowledge, loading = sending, fullWidth = true)
        }
    }
}
