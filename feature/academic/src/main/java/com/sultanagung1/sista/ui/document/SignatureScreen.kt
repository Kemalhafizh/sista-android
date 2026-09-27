package com.sultanagung1.sista.ui.document

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.ui.common.SignaturePad
import com.sultanagung1.sista.ui.common.SignatureStroke
import com.sultanagung1.sista.ui.common.captureSignatureAsBase64Png

/**
 * A real signature capture + submission flow (POST documents/signature/submit)
 * — this used to draw a real signature on screen but then discard it and show
 * an unconditional "success" dialog with no API call at all. Nothing in the
 * app currently navigates here with a real [documentType]/[documentId] (no
 * caller was ever wired up), so those stay nullable: without them there is no
 * real document to attach a signature to, and this screen says so honestly
 * instead of submitting a fabricated document id.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignatureScreen(
    documentType: String? = null,
    documentId: Long? = null,
    documentName: String = "Dokumen",
    viewModel: SignatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSignatureSaved: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val strokes = remember { mutableStateListOf<SignatureStroke>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val hasRealDocument = documentType != null && documentId != null

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSignatureSaved()
            onNavigateBack()
        }
    }

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
                    text = "Tanda tangan ini disimpan sebagai berkas digital dan terikat dengan ID akun Anda yang sedang login.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!hasRealDocument) {
                    Spacer(modifier = Modifier.height(10.dp))
                    SulaoneErrorBanner(message = "Layar ini belum terhubung ke dokumen spesifik mana pun, jadi tanda tangan tidak bisa disimpan dari sini.")
                }

                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(10.dp))
                    SulaoneErrorBanner(message = message)
                }

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
                    SignaturePad(
                        strokes = strokes,
                        onCanvasSized = { canvasSize = it },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (strokes.isEmpty()) {
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
                        onClick = { strokes.removeLastOrNull() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { strokes.clear() },
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
                text = if (uiState.isSubmitting) "Menyimpan..." else "Sahkan Dokumen Ini",
                onClick = {
                    if (documentType != null && documentId != null) {
                        captureSignatureAsBase64Png(strokes, canvasSize)?.let { base64 ->
                            viewModel.submit(documentType, documentId, base64)
                        }
                    }
                },
                enabled = hasRealDocument && strokes.isNotEmpty() && !uiState.isSubmitting,
                isLoading = uiState.isSubmitting,
                icon = Icons.Default.Verified
            )
        }
    }
}
