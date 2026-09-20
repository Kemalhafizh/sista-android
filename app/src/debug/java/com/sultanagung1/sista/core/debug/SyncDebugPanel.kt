package com.sultanagung1.sista.core.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.designsystem.Emerald500
import com.sultanagung1.sista.core.designsystem.Slate900
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDebugPanel(modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isExpanded by remember { mutableStateOf(true) }

    // State bindings for real-time updates
    var requestCount by remember { mutableStateOf(SistaNetworkInterceptor.requestCount.get()) }
    var bytesSent by remember { mutableStateOf(SistaNetworkInterceptor.bytesSent.get()) }
    var bytesReceived by remember { mutableStateOf(SistaNetworkInterceptor.bytesReceived.get()) }
    var isInterceptorEnabled by remember { mutableStateOf(SistaNetworkInterceptor.isEnabled.get()) }
    // Dummy online status for connection indicator
    var isOnline by remember { mutableStateOf(true) }

    // Polling effect for stats
    LaunchedEffect(isVisible) {
        while (isVisible) {
            requestCount = SistaNetworkInterceptor.requestCount.get()
            bytesSent = SistaNetworkInterceptor.bytesSent.get()
            bytesReceived = SistaNetworkInterceptor.bytesReceived.get()
            delay(1000)
        }
    }

    Box(modifier = modifier) {
        if (!isVisible) {
            FloatingActionButton(
                onClick = { isVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Slate900,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Build, contentDescription = "Open Debug Panel")
            }
        } else {
            Card(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .padding(16.dp)
                    .width(280.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.85f),
                    contentColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔧 SISTA Debug",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (isOnline) Emerald500 else AccentRose,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✕",
                            modifier = Modifier.clickable { isVisible = false },
                            color = Color.White
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Interceptor Enabled", modifier = Modifier.weight(1f), color = Color.White)
                            Switch(
                                checked = isInterceptorEnabled,
                                onCheckedChange = { 
                                    isInterceptorEnabled = it
                                    SistaNetworkInterceptor.isEnabled.set(it)
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Requests: $requestCount", color = Color.White)
                        Text("Sent: ${bytesSent / 1024} KB", color = Color.White)
                        Text("Received: ${bytesReceived / 1024} KB", color = Color.White)
                    }
                }
            }
        }
    }
}
