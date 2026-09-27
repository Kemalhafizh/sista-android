package com.sultanagung1.sista.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pengaturan Bahasa",
                subtitle = "Language & Locale Settings",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Pilih Bahasa Aplikasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Perubahan bahasa dan tata letak RTL akan diterapkan langsung secara real-time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language Options
            AppLanguage.values().forEach { lang ->
                val isSelected = lang == currentLanguage
                SulaoneCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.setLanguage(lang) }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Emerald700 else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    elevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Emerald100 else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = if (isSelected) Emerald800 else Slate500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = lang.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Emerald700),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Live Preview Card
            Text(
                text = "Pratinjau Teks & Tata Letak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald900)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = if (currentLanguage == AppLanguage.ARABIC) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.INDONESIAN -> "Assalamu'alaikum Warahmatullahi Wabarakatuh"
                            AppLanguage.ENGLISH -> "May Peace & Blessings be upon You"
                            AppLanguage.ARABIC -> "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللهِ وَبَرَكَاتُهُ"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gold400,
                        textAlign = if (currentLanguage == AppLanguage.ARABIC) TextAlign.Right else TextAlign.Left
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.INDONESIAN -> "Selamat datang di Sulaone SuperApp SMA Islam Sultan Agung 1 Semarang. Ekosistem pendidikan islami terpadu Kurikulum Merdeka."
                            AppLanguage.ENGLISH -> "Welcome to Sulaone SuperApp at Sultan Agung 1 Islamic High School. Integrated Islamic educational ecosystem."
                            AppLanguage.ARABIC -> "مرحبًا بكم في التطبيق الشامل لمدرسة السلطان أجونج 1 الإسلامية الثانوية بمدينة سيمارانج."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald100,
                        textAlign = if (currentLanguage == AppLanguage.ARABIC) TextAlign.Right else TextAlign.Left
                    )
                }
            }
        }
    }
}
