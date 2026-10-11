package com.sultanagung1.sista.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.feature.finance.R
import java.time.LocalDate
import java.util.Locale

/** "Tagihan": the student's bills as the school recorded them. Read only; paying happens at the treasurer. */
@Composable
fun BillingScreen(
    onNavigateBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    BillingContent(
        state = state,
        today = BillingRules.today(),
        onFilter = viewModel::setFilter,
        onRetry = viewModel::load,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun BillingContent(
    state: BillingUiState,
    today: LocalDate,
    onFilter: (BillingRules.Filter) -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = stringResource(R.string.billing_title), onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            val invoices = state.invoices
            when {
                invoices == null && state.isLoading ->
                    SkeletonList(Modifier.padding(padding).padding(Spacing.screen), rows = 4)
                invoices == null ->
                    ErrorState(
                        title = stringResource(R.string.billing_screen_load_failed),
                        body = state.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.padding(padding).fillMaxSize(),
                    )
                invoices.isEmpty() ->
                    EmptyState(
                        title = stringResource(R.string.billing_empty_title),
                        body = stringResource(R.string.billing_empty_body),
                        icon = Icons.Outlined.ReceiptLong,
                        modifier = Modifier.padding(padding).fillMaxSize(),
                    )
                else -> BillingList(state, invoices, today, onFilter, onRetry, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun BillingList(
    state: BillingUiState,
    invoices: List<BillingInvoice>,
    today: LocalDate,
    onFilter: (BillingRules.Filter) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val summary = BillingRules.summary(invoices)
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("billing_list"),
        contentPadding = PaddingValues(bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        state.errorMessage?.let { message ->
            item(key = "error") {
                InlineBanner(
                    message = message,
                    tone = StatusTone.Danger,
                    actionLabel = stringResource(R.string.billing_retry),
                    onAction = onRetry,
                    modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs),
                )
            }
        }
        item(key = "summary") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.screen, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                StatTile(
                    label = stringResource(R.string.billing_summary_remaining),
                    value = BillingRules.rupiah(summary.remainingTotal, locale),
                    supporting = stringResource(R.string.billing_summary_open_count, summary.openCount),
                    tone = if (summary.openCount > 0) StatusTone.Warning else StatusTone.Success,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.billing_summary_paid),
                    value = summary.paidCount.toString(),
                    supporting = stringResource(R.string.billing_summary_paid_hint),
                    tone = StatusTone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item(key = "how_to_pay") {
            InlineBanner(
                title = stringResource(R.string.billing_how_to_pay_title),
                message = stringResource(R.string.billing_how_to_pay_body),
                tone = StatusTone.Info,
                modifier = Modifier.padding(horizontal = Spacing.screen),
            )
        }
        item(key = "filter") {
            val labels = BillingRules.Filter.entries.associateWith { filterLabel(it) }
            FilterChipRow(
                options = BillingRules.Filter.entries,
                selected = state.filter,
                onSelect = onFilter,
                label = { labels.getValue(it) },
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
        val shown = state.shown
        if (shown.isEmpty()) {
            item(key = "filtered_empty") {
                EmptyState(
                    title = stringResource(if (state.filter == BillingRules.Filter.OPEN) R.string.billing_none_open else R.string.billing_none_paid),
                    icon = Icons.Outlined.TaskAlt,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            }
        }
        items(shown, key = { it.id }) { invoice ->
            InvoiceCard(invoice, today, locale, Modifier.padding(horizontal = Spacing.screen))
        }
    }
}

@Composable
private fun filterLabel(filter: BillingRules.Filter): String = stringResource(
    when (filter) {
        BillingRules.Filter.ALL -> R.string.billing_filter_all
        BillingRules.Filter.OPEN -> R.string.billing_filter_open
        BillingRules.Filter.PAID -> R.string.billing_filter_paid
    }
)

@Composable
private fun InvoiceCard(invoice: BillingInvoice, today: LocalDate, locale: Locale, modifier: Modifier = Modifier) {
    val status = BillingRules.statusOf(invoice)
    val overdue = BillingRules.isOverdue(invoice, today)
    SistaCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(end = Spacing.sm)) {
                Text(
                    invoice.category?.takeIf { it.isNotBlank() } ?: stringResource(R.string.billing_unknown),
                    style = SistaTheme.typography.titleMedium,
                )
                invoice.month?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant)
                }
            }
            when {
                status == BillingRules.Status.PAID -> StatusPill(stringResource(R.string.billing_status_paid), StatusTone.Success)
                overdue -> StatusPill(stringResource(R.string.billing_status_overdue), StatusTone.Danger)
                status == BillingRules.Status.PARTIAL -> StatusPill(stringResource(R.string.billing_status_partial), StatusTone.Warning)
                status == BillingRules.Status.UNPAID -> StatusPill(stringResource(R.string.billing_status_unpaid), StatusTone.Warning)
                else -> StatusPill(invoice.status, StatusTone.Neutral)
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Amount(stringResource(R.string.billing_amount), BillingRules.rupiah(invoice.amount, locale), Modifier.weight(1f))
            if (status != BillingRules.Status.PAID) {
                Amount(stringResource(R.string.billing_remaining), BillingRules.rupiah(invoice.remainingBalance, locale), Modifier.weight(1f))
            }
        }

        BillingRules.dueDateText(invoice.dueDate, locale)?.let { due ->
            Text(
                stringResource(R.string.billing_due, due),
                style = SistaTheme.typography.bodySmall,
                color = if (overdue) SistaTheme.colors.error else SistaTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

@Composable
private fun Amount(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = SistaTheme.typography.labelSmall, color = SistaTheme.colors.onSurfaceVariant)
        Text(value, style = SistaTheme.typography.titleMedium)
    }
}
