package com.sultanagung1.sista.ui.counseling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaListItem
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.repository.BullyingReportRepository
import com.sultanagung1.sista.feature.academic.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * "Lapor Perundungan" (FASE 78.2): a plain report to the school's
 * counsellors, anonymous unless the reporter says otherwise. It replaced a
 * "SOS" button whose countdown never reached anyone.
 */
@Composable
fun BullyingReportScreen(
    viewModel: BullyingReportViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    BullyingReportContent(
        state = state,
        today = BullyingReportRules.today(),
        onChange = viewModel::update,
        onSubmit = { viewModel.submit() },
        onNewReport = viewModel::reset,
        onNavigateBack = onNavigateBack,
    )
}

/** The report form and its confirmation, without a ViewModel, for previews and screenshots. */
@Composable
fun BullyingReportContent(
    state: BullyingReportUiState,
    today: LocalDate,
    onChange: ((BullyingReportForm) -> BullyingReportForm) -> Unit,
    onSubmit: () -> Unit,
    onNewReport: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    ShellTheme {
        Scaffold(
            topBar = { SistaTopBar(title = stringResource(R.string.bully_title), onBack = onNavigateBack) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                val submitted = state.submitted
                if (submitted != null) {
                    Submitted(submitted, onNewReport, onNavigateBack)
                } else {
                    ReportForm(state, today, onChange, onSubmit)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportForm(
    state: BullyingReportUiState,
    today: LocalDate,
    onChange: ((BullyingReportForm) -> BullyingReportForm) -> Unit,
    onSubmit: () -> Unit,
) {
    val form = state.form
    InlineBanner(
        title = stringResource(R.string.bully_intro_title),
        message = stringResource(R.string.bully_intro_body),
        tone = StatusTone.Info,
    )

    SectionHeader(stringResource(R.string.bully_what))
    SistaTextField(
        value = form.description,
        onValueChange = { text -> onChange { it.copy(description = text) } },
        label = stringResource(R.string.bully_description),
        placeholder = stringResource(R.string.bully_description_hint),
        helperText = stringResource(R.string.bully_count, form.description.trim().length, BullyingReportRules.MAX_DESCRIPTION),
        errorText = BullyingReportRules.descriptionError(form)?.takeIf { state.showErrors }?.asString(),
        singleLine = false,
        minLines = 5,
        enabled = !state.isSending,
        modifier = Modifier.fillMaxWidth(),
    )

    SectionHeader(stringResource(R.string.bully_when))
    DateChoice(form.incidentDate, today, enabled = !state.isSending) { date -> onChange { it.copy(incidentDate = date) } }
    BullyingReportRules.dateError(form, today)?.let { Text(it.asString(), color = SistaTheme.colors.error, style = SistaTheme.typography.bodySmall) }

    SistaTextField(
        value = form.location,
        onValueChange = { text -> onChange { it.copy(location = text) } },
        label = stringResource(R.string.bully_where),
        placeholder = stringResource(R.string.bully_where_hint),
        errorText = BullyingReportRules.locationError(form)?.asString(),
        enabled = !state.isSending,
        modifier = Modifier.fillMaxWidth(),
    )

    SectionHeader(stringResource(R.string.bully_type))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        BullyingReportRules.TYPES.forEach { (code, label) ->
            val selected = form.typeCode == code
            FilterChip(
                selected = selected,
                onClick = { onChange { it.copy(typeCode = if (selected) null else code) } },
                label = { Text(stringResource(label)) },
                leadingIcon = if (selected) ({ Icon(Icons.Outlined.Check, contentDescription = null) }) else null,
                enabled = !state.isSending,
            )
        }
    }
    Text(stringResource(R.string.bully_type_hint), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)

    SistaCard(modifier = Modifier.fillMaxWidth()) {
        SistaListItem(
            headline = stringResource(R.string.bully_anonymous),
            supporting = stringResource(if (form.anonymous) R.string.bully_anonymous_on else R.string.bully_anonymous_off),
            leading = { IconBadge(Icons.Outlined.Shield, tone = if (form.anonymous) StatusTone.Success else StatusTone.Neutral) },
            trailing = { Switch(checked = form.anonymous, onCheckedChange = null, enabled = !state.isSending) },
            onClick = if (state.isSending) null else ({ onChange { it.copy(anonymous = !it.anonymous) } }),
        )
    }

    state.errorMessage?.let { InlineBanner(message = it, tone = StatusTone.Danger) }

    SistaButton(
        stringResource(R.string.bully_send),
        onSubmit,
        leadingIcon = Icons.AutoMirrored.Outlined.Send,
        loading = state.isSending,
        enabled = !state.isSending,
        fullWidth = true,
    )
}

/** Today, yesterday, another day from a calendar, or not given. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateChoice(date: LocalDate?, today: LocalDate, enabled: Boolean, onDate: (LocalDate?) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val yesterday = today.minusDays(1)
    val other = date != null && date != today && date != yesterday
    val formatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM yyyy", displayLocale()) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        FilterChip(date == today, { onDate(if (date == today) null else today) }, { Text(stringResource(R.string.bully_today)) }, enabled = enabled)
        FilterChip(date == yesterday, { onDate(if (date == yesterday) null else yesterday) }, { Text(stringResource(R.string.bully_yesterday)) }, enabled = enabled)
        FilterChip(
            selected = other,
            onClick = { picking = true },
            label = { Text(if (other) date!!.format(formatter) else stringResource(R.string.bully_other_day)) },
            enabled = enabled,
        )
    }
    if (date == null) {
        Text(stringResource(R.string.bully_date_unset), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
    }
    if (picking) {
        val todayMillis = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: today).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    picking = false
                }) { Text(stringResource(R.string.bully_pick)) }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text(stringResource(R.string.bully_cancel)) } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun Submitted(
    submitted: BullyingReportRepository.Submitted,
    onNewReport: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val report = submitted.report
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.CheckCircle, tone = StatusTone.Success)
                Spacer(Modifier.width(Spacing.md))
                Text(stringResource(R.string.bully_sent_title), style = SistaTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                report.status?.let { StatusPill(it, StatusTone.Info) }
            }
            // The server's own words, in the user's language.
            submitted.message?.let { Text(it, style = SistaTheme.typography.bodyMedium) }
            report.ticketNumber?.let {
                Text(stringResource(R.string.bully_ticket, it), style = SistaTheme.typography.labelLarge)
            }
            Text(
                stringResource(if (report.isAnonymous) R.string.bully_sent_anonymous else R.string.bully_sent_named),
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
            )
        }
    }
    SistaButton(stringResource(R.string.bully_new), onNewReport, variant = ButtonVariant.Secondary, fullWidth = true)
    if (onNavigateBack != null) {
        SistaButton(stringResource(R.string.bully_done), onNavigateBack, variant = ButtonVariant.Text, fullWidth = true)
    }
}
