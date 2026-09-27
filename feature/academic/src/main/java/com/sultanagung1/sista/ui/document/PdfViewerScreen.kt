package com.sultanagung1.sista.ui.document

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Renders a real PDF (downloaded to app cache, paged via Android's built-in
 * PdfRenderer — no external library needed) instead of a static mockup of one
 * specific student's report card. [fileUrl] must be a real, reachable URL
 * (see Constants.resolveStorageUrl for backend relative-path resolution).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    fileUrl: String,
    documentTitle: String,
    sizeBytes: Long,
    onNavigateBack: () -> Unit,
    onDownloadPdf: () -> Unit = {}
) {
    val context = LocalContext.current
    var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDownloaded by remember { mutableStateOf(false) }

    LaunchedEffect(fileUrl) {
        isLoading = true
        errorMessage = null
        if (fileUrl.isBlank()) {
            isLoading = false
            errorMessage = "Tautan dokumen tidak tersedia."
            return@LaunchedEffect
        }
        try {
            val bitmaps = withContext(Dispatchers.IO) {
                val cacheFile = File(context.cacheDir, "preview_${fileUrl.hashCode()}.pdf")
                if (!cacheFile.exists()) {
                    java.net.URL(fileUrl).openStream().use { input ->
                        cacheFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                val pfd = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                PdfRenderer(pfd).use { renderer ->
                    (0 until renderer.pageCount).map { index ->
                        renderer.openPage(index).use { page ->
                            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap
                        }
                    }
                }
            }
            pageBitmaps = bitmaps
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Gagal memuat dokumen PDF: ${e.localizedMessage ?: "berkas tidak dapat dibuka"}"
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Penampil Dokumen PDF",
                subtitle = documentTitle,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (sizeBytes > 0) "${"%.1f".format(sizeBytes / 1_000_000.0)} MB" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            isDownloaded = true
                            onDownloadPdf()
                        },
                        enabled = errorMessage == null && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDownloaded) Emerald700 else Gold600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isDownloaded) "Diunduh ke Folder Unduhan" else "Unduh PDF")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Gold400)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Memuat dokumen...", color = androidx.compose.ui.graphics.Color.White)
                }

                errorMessage != null -> SulaoneErrorBanner(
                    message = errorMessage ?: "",
                    modifier = Modifier.padding(24.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pageBitmaps) { bitmap ->
                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Slate700, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = documentTitle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
