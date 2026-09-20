package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneHeading
import com.sultanagung1.sista.core.designsystem.Emerald100
import com.sultanagung1.sista.core.designsystem.Emerald50
import com.sultanagung1.sista.core.designsystem.Emerald700
import com.sultanagung1.sista.core.designsystem.Slate500
import com.sultanagung1.sista.ui.navigation.Screen

@Composable
internal fun HomeModuleCarousel(
    onNavigateToCatalog: () -> Unit,
    onNavigateRoute: ((String) -> Unit)?
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kesiswaan & Persiapan Kuliah",
                modifier = Modifier.sulaoneHeading("Kesiswaan & Persiapan Kuliah"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Semua Modul",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToCatalog() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val kesiswaanItems = listOf(
                Triple("Buku Saku Poin", "Pelanggaran & SP", Screen.Discipline.route),
                Triple("Simulasi UTBK", "Tryout IRT & PTN", Screen.UtbkTryout.route),
                Triple("E-Pustaka Pintar", "Katalog & Checkout", Screen.LibraryCatalog.route),
                Triple("Ekskul & OSIS", "Pendaftaran & Jadwal", Screen.Extracurricular.route),
                Triple("Portofolio Prestasi", "Upload Sertifikat Juara", Screen.AchievementUpload.route),
                Triple("Evaluasi Guru", "Kuesioner & E-Voting", Screen.TeacherEvaluation.route),
                Triple("Mode Hemat Kuota", "Optimasi RAM 2GB", Screen.LiteModeSettings.route)
            )

            items(kesiswaanItems) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                    modifier = Modifier
                        .width(150.dp)
                        .clickable { onNavigateRoute?.invoke(item.third) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Emerald50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item.first) {
                                    "Buku Saku Poin" -> Icons.Default.Gavel
                                    "Simulasi UTBK" -> Icons.Default.Psychology
                                    "E-Pustaka Pintar" -> Icons.Default.LocalLibrary
                                    "Ekskul & OSIS" -> Icons.Default.SportsSoccer
                                    "Portofolio Prestasi" -> Icons.Default.EmojiEvents
                                    "Evaluasi Guru" -> Icons.Default.HowToVote
                                    else -> Icons.Default.Speed
                                },
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.second,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
