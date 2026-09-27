package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SwipeableListItem(
    modifier: Modifier = Modifier,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = SulaoneMotion.SpringDefault,
        label = "swipeOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background Actions
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(if (offsetX > 0) Emerald600 else AccentRose),
            horizontalArrangement = if (offsetX > 0) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (offsetX > 0) {
                IconButton(onClick = {
                    onSwipeRight?.invoke()
                    offsetX = 0f
                }, modifier = Modifier.padding(start = 16.dp)) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "Selesai", tint = Color.White)
                }
            } else if (offsetX < 0) {
                IconButton(onClick = {
                    onSwipeLeft?.invoke()
                    offsetX = 0f
                }, modifier = Modifier.padding(end = 16.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White)
                }
            }
        }

        // Foreground Content
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        offsetX = (offsetX + delta).coerceIn(-200f, 200f)
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX > 100f) {
                            onSwipeRight?.invoke()
                        } else if (offsetX < -100f) {
                            onSwipeLeft?.invoke()
                        }
                        offsetX = 0f
                    }
                )
        ) {
            content()
        }
    }
}
