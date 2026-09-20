package com.sultanagung1.sista.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

data class ReportSubjectRow(
    val code: String,
    val name: String,
    val score: Int,
    val predicate: String,
    val kktp: Int = 75,
    val achievement: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    documentTitle: String = "Rapor Hasil Belajar Siswa (Fase F)",
    onNavigateBack: () -> Unit,
    onDownloadPdf: () -> Unit = {}
) {
    var isDownloaded by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableStateOf(1.0f) }

    val subjects = remember {
        listOf(
            ReportSubjectRow("PAI", "Pendidikan Agama & Budi Pekerti", 94, "A", 75, "Sangat menguasai pemahaman fiqih muamalah dan tajwid"),
            ReportSubjectRow("MTK", "Matematika Tingkat Lanjut", 90, "A", 75, "Sangat terampil dalam kalkulus diferensial dan matriks"),
            ReportSubjectRow("FIS", "Fisika Modern", 88, "A", 75, "Memahami prinsip mekanika kuantum dan gelombang"),
            ReportSubjectRow("BIO", "Biologi Molekuler", 92, "A", 75, "Sangat aktif dalam praktikum genetika dan bioteknologi"),
            ReportSubjectRow("KIM", "Kimia Terapan", 86, "B+", 75, "Menguasai termokimia dan laju reaksi kimia"),
            ReportSubjectRow("BIG", "Bahasa Inggris Akademik", 89, "A", 75, "Fasih dalam presentasi sains dan penulisan esai"),
            ReportSubjectRow("BAR", "Bahasa Arab & Tarjamah", 95, "A", 75, "Sangat baik dalam membaca kitab dan qawa'id")
        )
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "In-App PDF Document Viewer",
                subtitle = documentTitle,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Controls
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.1f).coerceAtLeast(0.8f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "${(zoomScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.1f).coerceAtMost(1.4f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Download / Share Button
                    Button(
                        onClick = {
                            isDownloaded = true
                            onDownloadPdf()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDownloaded) Emerald700 else Gold600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isDownloaded) "Tersimpan di Unduhan" else "Unduh PDF Resmi")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Slate950)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // A4 Document Page Simulation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Official Letterhead (Kop Surat YBWSA)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Emerald800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "YAYASAN BADAN WAKAF SULTAN AGUNG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                            Text(
                                text = "SMA ISLAM SULTAN AGUNG 1 SEMARANG",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate950
                            )
                            Text(
                                text = "Jl. Mataram No. 657 Semarang • Terakreditasi A (Unggul)",
                                fontSize = 9.sp,
                                color = Slate600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp, color = Slate900)
                    Spacer(modifier = Modifier.height(2.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Slate900)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = "LAPORAN HASIL CAPAIAN KOMPETENSI PESERTA DIDIK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Slate950
                    )
                    Text(
                        text = "Kurikulum Merdeka — Tahun Ajaran 2025/2026 Ganjil",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Student Metadata Table
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nama: Ahmad Kemal Hafizh", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("NISN: 0071829102", fontSize = 10.sp, color = Slate700)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kelas: XII MIPA 1 (Fase F)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("Wali Kelas: Drs. H. Ahmad Fauzi, M.Pd", fontSize = 10.sp, color = Slate700)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subjects Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Emerald800)
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(20.dp))
                        Text("Mata Pelajaran", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(2f))
                        Text("KKTP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("Nilai", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("Predikat", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                    }

                    // Subjects Rows
                    subjects.forEachIndexed { index, row ->
                        val rowBg = if (index % 2 == 0) Slate50 else Color.White
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}", fontSize = 9.sp, color = Slate900, modifier = Modifier.width(20.dp))
                            Column(modifier = Modifier.weight(2f)) {
                                Text(row.name, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Slate950)
                                Text(row.achievement, fontSize = 7.5.sp, color = Slate600, maxLines = 1)
                            }
                            Text("${row.kktp}", fontSize = 9.sp, color = Slate700, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            Text("${row.score}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Emerald900, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            Text(row.predicate, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold800, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Digital Verification Seal & Signatures
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mengetahui,", fontSize = 9.sp, color = Slate700)
                            Text("Wali Murid / Orang Tua", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(36.dp))
                            Text("( .................................... )", fontSize = 9.sp, color = Slate700)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Semarang, 20 Desember 2025", fontSize = 9.sp, color = Slate700)
                            Text("Kepala Sekolah", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Emerald50)
                                    .border(1.dp, Emerald400, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("TERVERIFIKASI DIGITAL", fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, color = Emerald800)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Drs. H. Muhammad Arif, M.Pd", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate950)
                            Text("NIP. 197405121998031002", fontSize = 8.sp, color = Slate600)
                        }
                    }
                }
            }
        }
    }
}
