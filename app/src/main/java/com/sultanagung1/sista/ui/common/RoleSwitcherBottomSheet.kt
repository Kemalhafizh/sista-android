package com.sultanagung1.sista.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.sultanagung1.sista.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

data class RoleOptionItem(
    val roleKey: String,
    val roleTitle: String,
    val roleDescription: String,
    val targetUser: String,
    val icon: ImageVector,
    val themeColor: Color,
    val backgroundColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitcherBottomSheet(
    currentRole: String,
    onRoleSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val roles = listOf(
        RoleOptionItem(
            roleKey = "student",
            roleTitle = "Siswa (Peserta Didik)",
            roleDescription = "Jadwal KBM, Presensi GPS & QR, Rapor KKTP, Ujian CBT Anti-Cheat, Mutaba'ah, AI Tutor",
            targetUser = "Ahmad Kemal Hafizh (XII MIPA 1)",
            icon = Icons.Default.School,
            themeColor = Emerald700,
            backgroundColor = Emerald50
        ),
        RoleOptionItem(
            roleKey = "teacher",
            roleTitle = "Guru (Pendidik)",
            roleDescription = "Presensi Kelas H/I/S/A, Jurnal KBM, Pengawas Ujian Live (Proctor), Buat Ulangan Daring",
            targetUser = "Ustadz Ahmad Fauzi, M.Pd (Guru Fisika)",
            icon = Icons.Default.CoPresent,
            themeColor = AccentBlue,
            backgroundColor = AccentBlue.copy(alpha = 0.12f)
        ),
        RoleOptionItem(
            roleKey = "parent",
            roleTitle = "Wali Murid (Orang Tua)",
            roleDescription = "Pantau Kehadiran Anak Live, Tagihan SPP & Virtual Account, Nilai Rapor, Chat WhatsApp Wali Kelas",
            targetUser = "Bapak Hendra Gunawan, S.T. (Wali dari Ahmad Kemal)",
            icon = Icons.Default.FamilyRestroom,
            themeColor = AccentAmber,
            backgroundColor = AccentAmber.copy(alpha = 0.12f)
        ),
        RoleOptionItem(
            roleKey = "admin",
            roleTitle = "Kepala Sekolah / Admin",
            roleDescription = "Executive Command Center, 4 Pilar KPI Sekolah, Approval SP3, Pengawasan Single VPS Server",
            targetUser = "Drs. H. Sukarno, M.Pd (Kepala SMA Sultan Agung 1)",
            icon = Icons.Default.AdminPanelSettings,
            themeColor = AccentPurple,
            backgroundColor = AccentPurple.copy(alpha = 0.12f)
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_kotak),
                        contentDescription = "Logo",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ganti Peran Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SuperApp Terpadu SMA Islam Sultan Agung 1 Semarang",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            roles.forEach { item ->
                val isSelected = currentRole.equals(item.roleKey, ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) item.backgroundColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) item.themeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable {
                            onRoleSelected(item.roleKey)
                            onDismissRequest()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) item.themeColor else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.roleTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) item.themeColor else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = item.themeColor
                                    ) {
                                        Text(
                                            text = "AKTIF",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = item.targetUser,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.roleDescription,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Slate500,
                                lineHeight = 15.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = item.themeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
