package com.sultanagung1.sista.ui.document

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.document.OcrProcessor
import com.sultanagung1.sista.data.model.OcrScanResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedDocType by remember { mutableStateOf("ASSIGNMENT") }
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<OcrScanResult?>(null) }

    val docTypes = listOf(
        "ASSIGNMENT" to "Tugas Siswa",
        "RECEIPT" to "Kuitansi SPP",
        "LETTER" to "Surat Edaran"
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pemindai Dokumen & OCR",
                subtitle = "Ekstraksi Teks Cerdas & Digitalisasi Berkas",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Slate950)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Document Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    docTypes.forEach { (type, label) ->
                        val isSelected = selectedDocType == type
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Emerald700 else Color.White.copy(alpha = 0.12f))
                                .clickable {
                                    selectedDocType = type
                                    scanResult = null
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Slate300
                            )
                        }
                    }
                }

                // Document Frame Viewfinder Canvas
                Box(
                    modifier = Modifier
                        .size(width = 280.dp, height = 380.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val strokeW = 4.dp.toPx()
                        val cornerLen = 36.dp.toPx()
                        val color = if (isScanning) Gold400 else Emerald400

                        // Top-Left
                        drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
                        drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

                        // Top-Right
                        drawLine(color, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeW)
                        drawLine(color, Offset(w, 0f), Offset(w, cornerLen), strokeW)

                        // Bottom-Left
                        drawLine(color, Offset(0f, h), Offset(cornerLen, h), strokeW)
                        drawLine(color, Offset(0f, h), Offset(0f, h - cornerLen), strokeW)

                        // Bottom-Right
                        drawLine(color, Offset(w, h), Offset(w - cornerLen, h), strokeW)
                        drawLine(color, Offset(w, h), Offset(w, h - cornerLen), strokeW)
                    }

                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Gold400,
                            modifier = Modifier.size(54.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = Slate600.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                // Action Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Arahkan kamera tegak lurus ke lembar dokumen",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SulaoneButton(
                        text = if (isScanning) "Mengekstrak Teks OCR..." else "Pindai Dokumen Sekarang",
                        onClick = {
                            isScanning = true
                            coroutineScope.launch {
                                val result = OcrProcessor.processDocumentImage(selectedDocType)
                                scanResult = result
                                isScanning = false
                            }
                        },
                        isLoading = isScanning,
                        icon = Icons.Default.CameraAlt
                    )
                }
            }

            // OCR Extraction Result Dialog
            scanResult?.let { result ->
                AlertDialog(
                    onDismissRequest = { scanResult = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teks Berhasil Diekstrak", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald50)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Akurasi OCR:", fontSize = 11.sp, color = Emerald900)
                                Text("${(result.confidence * 100).toInt()}% PASSED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            result.extractedFields.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(k, fontSize = 11.sp, color = Slate600)
                                    Text(v, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.scannedText,
                                fontSize = 10.sp,
                                color = Slate700,
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { scanResult = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Simpan ke Berkas")
                        }
                    }
                )
            }
        }
    }
}
