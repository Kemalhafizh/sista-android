package com.sultanagung1.sista.ui.finance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.BillingInvoice

/**
 * FASE 67: Overhauled Keuangan & Tagihan SPP Siswa Screen.
 * Modern Fintech / Neobank aesthetic: Clean off-white canvas (Slate50), flat 0dp cards,
 * 0.5dp borders (Slate200/Slate800), high-contrast WCAG 2.2 AA typography (Slate900),
 * Emerald600 accents, tactile haptics, and accessible 48dp touch targets.
 */
@Composable
fun BillingScreen(
    viewModel: BillingViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    val uiState by viewModel.uiState.collectAsState()
    val filteredInvoices by viewModel.filteredInvoices.collectAsState()
    val vaState by viewModel.vaState.collectAsState()
    val studentName by viewModel.studentName.collectAsState()
    val studentClass by viewModel.studentClass.collectAsState()

    var selectedInvoiceForVa by remember { mutableStateOf<BillingInvoice?>(null) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<BillingInvoice?>(null) }
    var selectedBankChannel by remember { mutableStateOf("BCA") }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Keuangan & SPP Siswa",
                subtitle = "Sistem Pembayaran Terpadu YBWSA",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
        ) {
            when (val state = uiState) {
                is BillingUiState.Loading -> {
                    BentoSkeletonLoader(modifier = Modifier.fillMaxSize())
                }
                is BillingUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SulaoneErrorBanner(
                            message = state.message,
                            onRetry = {
                                haptics.tapLight()
                                viewModel.loadBillings()
                            }
                        )
                    }
                }
                is BillingUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // =========================================================
                        // Modern Fintech Financial Summary Card (0dp Flat, 0.5dp Border)
                        // =========================================================
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Slate850 else Color.White
                                ),
                                border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    // Top row: Label & School Year Pill
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (state.totalUnpaid > 0) AccentAmber else Emerald600)
                                            )
                                            Text(
                                                text = "TOTAL TUNGGAKAN AKTIF",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Slate400 else Slate500,
                                                letterSpacing = 0.8.sp
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isDark) Slate800 else Slate100,
                                            border = BorderStroke(0.5.dp, if (isDark) Slate700 else Slate200)
                                        ) {
                                            Text(
                                                text = "T.A. 2026/2027",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isDark) Slate300 else Slate600,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Main Amount Display
                                    Text(
                                        text = "Rp ${"%,.0f".format(state.totalUnpaid).replace(',', '.')}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color.White else Slate900
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = if (isDark) Slate400 else Slate500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        val nearestDueDate = state.invoices
                                            .filter { it.status != BillingStatus.PAID }
                                            .minByOrNull { it.dueDate }?.dueDate
                                        Text(
                                            text = nearestDueDate?.let { "Jatuh tempo terdekat: $it" } ?: "Tidak ada tagihan tertunda",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Slate400 else Slate500
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = if (isDark) Slate800 else Slate200
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Bottom row: Total Paid & Student Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Total Pembayaran Lunas",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDark) Slate400 else Slate500
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Rp ${"%,.0f".format(state.totalPaid).replace(',', '.')}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Emerald300 else Emerald600
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Status Siswa",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDark) Slate400 else Slate500
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isDark) Slate800 else Emerald50,
                                                border = BorderStroke(0.5.dp, if (isDark) Slate700 else Emerald200)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Emerald600)
                                                    )
                                                    Text(
                                                        text = "Aktif Belajar",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDark) Emerald300 else Emerald700
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // =========================================================
                        // Minimalist Filter Tabs (0.5dp Border, 48dp Touch Targets)
                        // =========================================================
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "SEMUA" to "Semua (${state.invoices.size})",
                                    "UNPAID" to "Belum Bayar (${state.invoices.count { it.status != BillingStatus.PAID }})",
                                    "PAID" to "Lunas (${state.invoices.count { it.status == BillingStatus.PAID }})"
                                ).forEach { (key, label) ->
                                    val isSelected = state.selectedFilter == key
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) {
                                            Emerald600
                                        } else {
                                            if (isDark) Slate850 else Color.White
                                        },
                                        border = BorderStroke(
                                            0.5.dp,
                                            if (isSelected) Emerald600 else (if (isDark) Slate800 else Slate200)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .sulaoneInteractiveTouchTarget(48.dp)
                                            .springPressable {
                                                haptics.tapLight()
                                                viewModel.setFilter(key)
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else (if (isDark) Slate300 else Slate700)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // =========================================================
                        // Invoices List (Flat 0dp Card, 0.5dp Border, Micro-interactions)
                        // =========================================================
                        items(filteredInvoices) { item ->
                            val isPaid = item.status == BillingStatus.PAID

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Slate850 else Color.White
                                ),
                                border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Row 1: Title & Status Chip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.displayTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Slate900
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Event,
                                                    contentDescription = null,
                                                    tint = if (isDark) Slate400 else Slate500,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Batas Pembayaran: ${item.dueDate}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isDark) Slate400 else Slate500
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isPaid) {
                                                if (isDark) Slate800 else Emerald50
                                            } else {
                                                if (isDark) Slate800 else Gold50
                                            },
                                            border = BorderStroke(
                                                0.5.dp,
                                                if (isPaid) Emerald200 else AccentAmber.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.semantics(mergeDescendants = true) {
                                                stateDescription = if (isPaid) "Status Pembayaran: LUNAS" else "Status Pembayaran: BELUM BAYAR"
                                            }
                                        ) {
                                            Text(
                                                text = if (isPaid) "LUNAS" else "BELUM BAYAR",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isPaid) Emerald700 else AccentAmber,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = if (isDark) Slate800 else Slate100
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Row 2: Amount & Action Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Nominal Tagihan",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDark) Slate400 else Slate500
                                            )
                                            Text(
                                                text = item.amountFormatted,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isPaid) (if (isDark) Emerald300 else Emerald700) else (if (isDark) Color.White else Slate900)
                                            )
                                        }

                                        if (isPaid) {
                                            OutlinedButton(
                                                onClick = {
                                                    haptics.tapLight()
                                                    selectedInvoiceForReceipt = item
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(0.5.dp, if (isDark) Slate700 else Slate300),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = if (isDark) Emerald300 else Emerald700
                                                ),
                                                modifier = Modifier
                                                    .sulaoneInteractiveTouchTarget(48.dp)
                                                    .springPressable()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Receipt,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Kwitansi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    haptics.tapLight()
                                                    selectedInvoiceForVa = item
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Emerald600,
                                                    contentColor = Color.White
                                                ),
                                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                                modifier = Modifier
                                                    .sulaoneInteractiveTouchTarget(48.dp)
                                                    .springPressable()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Payment,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Bayar via VA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }

    // =====================================================================
    // VIRTUAL ACCOUNT PAYMENT DIALOG (Modern Fintech Theme)
    // =====================================================================
    if (selectedInvoiceForVa != null) {
        val invoice = selectedInvoiceForVa!!

        LaunchedEffect(invoice.id, selectedBankChannel) {
            viewModel.requestVa(invoice.id, selectedBankChannel)
        }
        DisposableEffect(Unit) {
            onDispose { viewModel.clearVaState() }
        }

        AlertDialog(
            onDismissRequest = {
                selectedInvoiceForVa = null
                viewModel.clearVaState()
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Slate850 else Color.White,
            title = {
                Text(
                    text = "Kanal Virtual Account (VA)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Slate900
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = invoice.displayTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isDark) Emerald300 else Emerald700
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total Tagihan: ${invoice.amountFormatted}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Slate900
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Pilih Bank Pembayaran:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Slate400 else Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Bank codes match exactly what ApiStudentController::payBilling() recognizes.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "BCA" to "BCA",
                            "MANDIRI" to "Mandiri",
                            "BNI" to "BNI",
                            "BRI" to "BRI"
                        ).forEach { (code, label) ->
                            val isSelected = selectedBankChannel == code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) {
                                    Emerald600
                                } else {
                                    if (isDark) Slate800 else Slate100
                                },
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isSelected) Emerald600 else (if (isDark) Slate700 else Slate200)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .sulaoneInteractiveTouchTarget(48.dp)
                                    .springPressable {
                                        haptics.tapLight()
                                        selectedBankChannel = code
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else (if (isDark) Slate300 else Slate700),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // VA Box — driven by the real POST .../billings/{id}/pay response
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Slate800 else Emerald50,
                        border = BorderStroke(0.5.dp, if (isDark) Slate700 else Emerald200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (val va = vaState) {
                                is VaRequestState.Loading, VaRequestState.Idle -> {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Emerald600)
                                }
                                is VaRequestState.Error -> {
                                    Text(
                                        text = va.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentRose,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                is VaRequestState.Success -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "NOMOR VIRTUAL ACCOUNT:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Emerald300 else Emerald700
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = va.va.vaNumber,
                                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDark) Color.White else Emerald900,
                                                letterSpacing = 1.sp
                                            )
                                            IconButton(
                                                onClick = {
                                                    haptics.success()
                                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("VA Number", va.va.vaNumber))
                                                    Toast.makeText(context, "Nomor VA berhasil disalin!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Salin Nomor VA",
                                                    tint = if (isDark) Emerald300 else Emerald600
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Atas Nama: ${studentName ?: "—"}${studentClass?.let { " ($it)" } ?: ""}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isDark) Slate400 else Slate600
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Berlaku hingga: ${va.va.expiryTime}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isDark) Slate400 else Slate600
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Panduan Pembayaran Singkat:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Slate300 else Slate700
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Buka Mobile Banking / ATM bank pilihan Anda.\n2. Pilih menu Pembayaran / Bayar Tagihan > Institusi Akademik.\n3. Masukkan nomor VA di atas.\n4. Konfirmasi nama siswa & jumlah, lalu selesaikan pembayaran.",
                        fontSize = 11.sp,
                        color = if (isDark) Slate400 else Slate600,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                val currentVa = (vaState as? VaRequestState.Success)?.va
                Button(
                    onClick = {
                        haptics.success()
                        currentVa?.let {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("VA Number", it.vaNumber))
                            Toast.makeText(context, "Nomor VA disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        }
                        selectedInvoiceForVa = null
                        viewModel.clearVaState()
                    },
                    enabled = currentVa != null,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                ) {
                    Text("Salin Nomor VA & Tutup", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptics.tapLight()
                        selectedInvoiceForVa = null
                        viewModel.clearVaState()
                    },
                    modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                ) {
                    Text("Tutup", color = if (isDark) Slate300 else Slate600)
                }
            }
        )
    }

    // =====================================================================
    // OFFICIAL DIGITAL RECEIPT / KUITANSI MODAL (Modern Fintech)
    // =====================================================================
    if (selectedInvoiceForReceipt != null) {
        val invoice = selectedInvoiceForReceipt!!
        val receiptNumber = "KW-YBWSA-2026-${invoice.id.toString().padStart(5, '0')}"

        Dialog(onDismissRequest = { selectedInvoiceForReceipt = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Slate850 else Color.White,
                border = BorderStroke(0.5.dp, if (isDark) Slate700 else Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Receipt Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Emerald600),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SA", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KUITANSI PEMBAYARAN RESMI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = if (isDark) Color.White else Emerald900
                            )
                            Text(
                                text = "SMA Islam Sultan Agung 1 Semarang (YBWSA)",
                                fontSize = 10.sp,
                                color = if (isDark) Slate400 else Slate500
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = if (isDark) Slate800 else Slate200
                    )

                    // Receipt Info Table
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("No. Kuitansi:", fontSize = 11.sp, color = if (isDark) Slate400 else Slate500)
                        Text(receiptNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (isDark) Color.White else Slate900)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tanggal Lunas:", fontSize = 11.sp, color = if (isDark) Slate400 else Slate500)
                        Text(invoice.dueDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Slate900)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nama Siswa:", fontSize = 11.sp, color = if (isDark) Slate400 else Slate500)
                        Text(studentName ?: "—", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Slate900)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Keterangan:", fontSize = 11.sp, color = if (isDark) Slate400 else Slate500)
                        Text(invoice.displayTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Slate900)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount Box with Paid Stamp
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Slate800 else Emerald50,
                        border = BorderStroke(0.5.dp, if (isDark) Slate700 else Emerald200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL TELAH DIBAYAR",
                                    fontSize = 9.sp,
                                    color = if (isDark) Emerald300 else Emerald700,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = invoice.amountFormatted,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Emerald900
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Slate700 else Emerald100,
                                border = BorderStroke(0.5.dp, Emerald600)
                            ) {
                                Text(
                                    text = "LUNAS • SAH",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Emerald300 else Emerald700,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // No backend endpoint exists yet to generate/download a receipt PDF
                    // for the mobile client (see ApiStudentController — payBilling() is a
                    // VA simulator with no persisted transaction to issue a receipt for).
                    Text(
                        text = "Fitur unduh kuitansi PDF belum tersedia di aplikasi ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate400 else Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
