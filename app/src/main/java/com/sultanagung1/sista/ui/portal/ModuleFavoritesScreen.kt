package com.sultanagung1.sista.ui.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sultanagung1.sista.data.model.EnterpriseModuleItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleFavoritesScreen(
    onNavigateToRoute: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val favoriteModules: List<EnterpriseModuleItem> = remember {
        listOf(
            EnterpriseModuleItem("m4", 4, "Presensi GPS", "Presensi radius 250m", "Presensi & IoT", "geofence_attendance", "LocationOn"),
            EnterpriseModuleItem("m5", 5, "QR Presensi Siswa", "Kode TOTP anti-joki", "Presensi & Akses", "dynamic_qr", "QrCode2"),
            EnterpriseModuleItem("m9", 9, "Sultan AI Tutor", "Bimbingan 24/7", "AI & Web3", "ai_tutor", "SmartToy"),
            EnterpriseModuleItem("m18", 18, "Rapor KKTP PDF", "Dokumen resmi YBWSA", "Akademik & LMS", "pdf_viewer", "PictureAsPdf"),
            EnterpriseModuleItem("m22", 22, "Radar Kompetensi", "Spider chart 6-sumbu", "Akademik & LMS", "academic_analytics", "AutoGraph"),
            EnterpriseModuleItem("m12", 12, "Mutabaah Yaumiyah", "Amalan sunnah harian", "Kesiswaan & Ibadah", "mutabaah", "Mosque")
        )
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Modul Favorit Saya",
                subtitle = "Pintasan Cepat Layanan Unggulan",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteModules, key = { it.id }) { item ->
                    SulaoneCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRoute(item.route) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Emerald100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Gold600,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
