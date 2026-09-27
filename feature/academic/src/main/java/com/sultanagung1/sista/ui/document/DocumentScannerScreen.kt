package com.sultanagung1.sista.ui.document

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sultanagung1.sista.core.designsystem.*

/**
 * Uploads a real picked image to documents/ocr/scan and polls for the
 * async result (DocumentScannerViewModel / DocumentRepository) — this used
 * to be entirely client-side fake data returned instantly regardless of what
 * (if anything) the user picked. The extracted text itself is still honestly
 * a placeholder on the backend (ProcessOcrDocument job simulates OCR, no
 * real recognition engine is wired up yet), but the upload, queueing, and
 * status polling are all real now.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    viewModel: DocumentScannerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedDocType by remember { mutableStateOf("ASSIGNMENT") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val docTypes = listOf(
        "ASSIGNMENT" to "Tugas Siswa",
        "RECEIPT" to "Kuitansi SPP",
        "LETTER" to "Surat Edaran"
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        selectedImageUri = uri
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            viewModel.scanDocument(bytes, "scan_${System.currentTimeMillis()}.jpg", mimeType, selectedDocType)
        }
    }

    val isBusy = uiState.isUploading || uiState.isPolling

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pemindai Dokumen & OCR",
                subtitle = "Ekstraksi Teks & Digitalisasi Berkas",
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
                                .clickable(enabled = !isBusy) { selectedDocType = type }
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
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Dokumen terpilih",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val strokeW = 4.dp.toPx()
                        val cornerLen = 36.dp.toPx()
                        val color = if (isBusy) Gold400 else Emerald400

                        drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
                        drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)
                        drawLine(color, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeW)
                        drawLine(color, Offset(w, 0f), Offset(w, cornerLen), strokeW)
                        drawLine(color, Offset(0f, h), Offset(cornerLen, h), strokeW)
                        drawLine(color, Offset(0f, h), Offset(0f, h - cornerLen), strokeW)
                        drawLine(color, Offset(w, h), Offset(w - cornerLen, h), strokeW)
                        drawLine(color, Offset(w, h), Offset(w, h - cornerLen), strokeW)
                    }

                    if (isBusy) {
                        CircularProgressIndicator(color = Gold400, modifier = Modifier.size(54.dp))
                    } else if (selectedImageUri == null) {
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
                        text = when {
                            uiState.isUploading -> "Mengunggah dokumen..."
                            uiState.isPolling -> "Memproses OCR di server..."
                            else -> "Pilih foto dokumen dari galeri untuk dipindai"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SulaoneButton(
                        text = if (isBusy) "Memproses..." else "Pilih & Pindai Dokumen",
                        onClick = {
                            if (!isBusy) {
                                viewModel.clearResult()
                                imagePicker.launch("image/*")
                            }
                        },
                        isLoading = isBusy,
                        icon = Icons.Default.CameraAlt
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearResult() },
                    icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose) },
                    title = { Text("Gagal Memindai", fontWeight = FontWeight.Bold) },
                    text = { Text(message) },
                    confirmButton = {
                        Button(onClick = { viewModel.clearResult() }) { Text("Tutup") }
                    }
                )
            }

            // OCR Result Dialog — shows the real backend response honestly,
            // including that recognition itself is still a placeholder.
            uiState.result?.let { result ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearResult() },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.status == "completed") Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (result.status == "completed") Emerald700 else AccentRose,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (result.status == "completed") "Dokumen Diproses" else "Pemrosesan Gagal",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column {
                            val extractedText = result.extractedText
                            if (result.status == "completed" && !extractedText.isNullOrBlank()) {
                                Text(
                                    text = extractedText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            } else if (result.status != "completed") {
                                Text(
                                    text = "Server gagal memproses dokumen ini. Coba unggah ulang.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.clearResult() },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Selesai")
                        }
                    }
                )
            }
        }
    }
}
