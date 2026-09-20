package com.sultanagung1.sista.ui.document

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

data class DrawPath(
    val path: Path,
    val color: Color = Color.Black,
    val strokeWidth: Float = 6f
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignatureScreen(
    documentName: String = "Persetujuan Rapor & Buku Induk Siswa",
    onNavigateBack: () -> Unit,
    onSignatureSaved: () -> Unit = {}
) {
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Tanda Tangan Digital Resmi",
                subtitle = documentName,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Bubuhi Tanda Tangan pada Kotak di Bawah",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tanda tangan ini sah secara hukum dan terikat dengan ID autentikasi pengguna YBWSA.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Pad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, Emerald700, RoundedCornerShape(16.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInteropFilter { motionEvent ->
                                when (motionEvent.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        val p = Path().apply { moveTo(motionEvent.x, motionEvent.y) }
                                        currentPath = p
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        currentPath?.lineTo(motionEvent.x, motionEvent.y)
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        currentPath?.let {
                                            paths.add(DrawPath(path = it, color = Slate950, strokeWidth = 6f))
                                        }
                                        currentPath = null
                                    }
                                }
                                true
                            }
                    ) {
                        paths.forEach { drawPath ->
                            drawPath(
                                path = drawPath.path,
                                color = drawPath.color,
                                style = Stroke(
                                    width = drawPath.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        currentPath?.let { p ->
                            drawPath(
                                path = p,
                                color = Slate950,
                                style = Stroke(
                                    width = 6f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (paths.isEmpty() && currentPath == null) {
                        Text(
                            text = "Sentuh & goreskan tanda tangan di sini...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons (Clear / Undo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (paths.isNotEmpty()) paths.removeLastOrNull()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { paths.clear() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bersihkan")
                    }
                }
            }

            // Submit Button
            SulaoneButton(
                text = "Sahkan Dokumen Ini",
                onClick = {
                    if (paths.isNotEmpty()) {
                        showSuccessDialog = true
                    }
                },
                enabled = paths.isNotEmpty(),
                icon = Icons.Default.Verified
            )
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSignatureSaved()
                onNavigateBack()
            },
            icon = {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Emerald700, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Tanda Tangan Terverifikasi", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Tanda tangan digital Anda telah berhasil direkam dan disahkan ke dalam sistem administrasi SMA Islam Sultan Agung 1 Semarang.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSignatureSaved()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Selesai")
                }
            }
        )
    }
}
