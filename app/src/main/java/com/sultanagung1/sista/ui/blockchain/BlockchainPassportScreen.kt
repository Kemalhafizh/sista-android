package com.sultanagung1.sista.ui.blockchain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.BlockchainCredentialItem

@Composable
fun BlockchainPassportScreen(
    onNavigateBack: () -> Unit
) {
    val sampleCredentials = listOf(
        BlockchainCredentialItem(
            id = 1,
            certificateNumber = "SULA-DIPLOMA-2026-0042",
            title = "Ijazah Kelulusan SMA Islam Sultan Agung 1",
            issuer = "YBWSA • Dinas Pendidikan Prov. Jateng",
            issueDate = "20 Juni 2026",
            txHash = "0x8f2b3e4a5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f",
            isVerified = true
        ),
        BlockchainCredentialItem(
            id = 2,
            certificateNumber = "SULA-TAHFIDZ-30JUZ-019",
            title = "Sertifikat Sanad Tahfidz Al-Qur'an 30 Juz",
            issuer = "Lembaga Pengembangan Tahfidz Sultan Agung",
            issueDate = "15 Mei 2026",
            txHash = "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
            isVerified = true
        ),
        BlockchainCredentialItem(
            id = 3,
            certificateNumber = "SULA-OLYMPIAD-PHYS-2026",
            title = "Medali Emas Olimpiade Sains Nasional (Fisika)",
            issuer = "Pusat Prestasi Nasional Kemendikbudristek",
            issueDate = "10 April 2026",
            txHash = "0x3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d",
            isVerified = true
        )
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Paspor Digital Web3",
                subtitle = "Verifiable Credentials & DID Siswa Sultan Agung",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // DID Card
                SulaoneGradientCard(
                    brush = Brush.linearGradient(listOf(Slate950, Slate800))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DECENTRALIZED IDENTIFIER (DID)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Gold400
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "did:sulaone:0x892a...4b19",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Terdaftar di Sulaone Private Consortium Ledger",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald300
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Emerald800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Gold400, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Ijazah & Sertifikat Terverifikasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(sampleCredentials) { item ->
                SulaoneCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Emerald100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Emerald800,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Penerbit: ${item.issuer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TX: ${item.txHash.take(18)}...",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = Slate500
                            )
                        }

                        SulaoneBadge(
                            text = "IMMUTABLE",
                            containerColor = Emerald100,
                            contentColor = Emerald800
                        )
                    }
                }
            }
        }
    }
}
