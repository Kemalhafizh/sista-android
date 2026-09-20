package com.sultanagung1.sista.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

/**
 * Route-Level Security Guard for Jetpack Compose Navigation.
 *
 * Prevents Unauthorized Role Traversal and UI/UX Privilege Escalation.
 * If the current user's role does not match one of [allowedRoles],
 * an "Akses Ditolak (403 Forbidden)" UI is rendered instead of the privileged screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleGuardedScreen(
    currentRole: String?,
    allowedRoles: List<String>,
    featureTitle: String = "Fitur Terbatas",
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    content: @Composable () -> Unit
) {
    val normalizedCurrentRole = currentRole?.trim()?.lowercase() ?: "student"
    
    val isAuthorized = normalizedCurrentRole == "superadmin" || allowedRoles.any { role ->
        normalizedCurrentRole.contains(role.trim().lowercase())
    }

    if (isAuthorized) {
        content()
    } else {
        Scaffold(
            topBar = {
                SulaoneTopBar(
                    title = "Akses Ditolak (403)",
                    subtitle = featureTitle,
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Shield Icon with Warning Tint
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(AccentRose.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Akses Ditolak",
                                tint = AccentRose,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Otorisasi Diperlukan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Anda tidak memiliki hak akses untuk membuka modul '$featureTitle'. Modul ini dilindungi oleh kebijakan keamanan peran (Role-Based Access Control).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Peran Anda Saat Ini:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Slate500
                                    )
                                    Text(
                                        text = normalizedCurrentRole.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentRose
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Peran yang Diizinkan:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Slate500
                                    )
                                    Text(
                                        text = allowedRoles.joinToString(", ") { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald700
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kembali")
                            }

                            Button(
                                onClick = onNavigateHome,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Beranda")
                            }
                        }
                    }
                }
            }
        }
    }
}
