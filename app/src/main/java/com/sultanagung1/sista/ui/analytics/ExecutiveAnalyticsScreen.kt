package com.sultanagung1.sista.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.ui.analytics.components.AnimatedKpiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveAnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPdf: () -> Unit = {}
) {
    val execData by viewModel.executiveKpi.collectAsState()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Dashboard Eksekutif & KPI",
                subtitle = "Yayasan Badan Wakaf Sultan Agung",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "INDIKATOR KINERJA UTAMA (KPI)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            execData?.kpiList?.forEach { kpi ->
                AnimatedKpiCard(kpi = kpi)
            }

            Spacer(modifier = Modifier.height(10.dp))

            SulaoneButton(
                text = "Cetak Ringkasan Eksekutif (PDF)",
                onClick = onNavigateToPdf,
                icon = Icons.Default.PictureAsPdf
            )
        }
    }
}
