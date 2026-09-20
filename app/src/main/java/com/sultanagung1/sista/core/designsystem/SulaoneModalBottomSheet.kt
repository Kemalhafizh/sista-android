package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * FASE 70.2 — Sulaone's atomic modal bottom sheet. Supersedes the old
 * `SulaoneBottomSheet` (same single real caller, migrated) with two things
 * the roadmap calls for that the old one didn't have:
 *
 * 1. [fullHeight] — when true, the sheet skips the half-expanded anchor and
 *    opens straight to (near-)full height, for screens like
 *    "Semua Layanan SISTA" whose list is too long to fit half a screen.
 *    When false (default), the sheet starts at its natural content height
 *    and the user can still drag it further — M3's `SheetState` already
 *    animates every drag/settle transition with a real spring
 *    (`AnchoredDraggableState` + `SpringSpec`), so this doesn't reinvent that.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SulaoneModalBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    fullHeight: Boolean = false,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = fullHeight)

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (fullHeight) Modifier.fillMaxHeight() else Modifier)
                    .padding(bottom = 24.dp)
            ) {
                if (title != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    content()
                }
            }
        }
    }
}

/**
 * The sheet's own drag-to-dismiss gesture already covers this whole handle
 * area via M3's `SheetState`/`anchoredDraggable`; this composable only
 * supplies the visual, so it doesn't add a second gesture detector here that
 * could steal the touch-down event from the sheet's real drag handling.
 */
@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .width(32.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(Slate300)
    )
}
