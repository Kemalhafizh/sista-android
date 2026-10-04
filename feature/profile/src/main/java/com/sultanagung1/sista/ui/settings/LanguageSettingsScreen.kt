package com.sultanagung1.sista.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.feature.profile.R

/**
 * Indonesian, English or Arabic, as on the web. The choice applies at once
 * (Arabic right-to-left) and is saved on the account so the web follows it.
 */
@Composable
fun LanguageSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val current by viewModel.currentLanguage.collectAsState()
    LanguageSettingsContent(current = current, onSelect = viewModel::setLanguage, onNavigateBack = onNavigateBack)
}

/** What each language is called in the language showing now ("Arabic", "العربية"…). */
@StringRes
internal fun languageName(language: AppLanguage): Int = when (language) {
    AppLanguage.INDONESIAN -> R.string.lang_name_id
    AppLanguage.ENGLISH -> R.string.lang_name_en
    AppLanguage.ARABIC -> R.string.lang_name_ar
}

/** The language page without a ViewModel, for previews and screenshots. */
@Composable
fun LanguageSettingsContent(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = stringResource(R.string.lang_title), onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("language_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "languages") {
                    SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                        AppLanguage.entries.forEachIndexed { index, language ->
                            if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = language == current, role = Role.RadioButton, onClick = { onSelect(language) })
                                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = language == current, onClick = null)
                                Spacer(Modifier.width(Spacing.md))
                                Column(Modifier.weight(1f)) {
                                    // The language's own name, so anyone can find theirs.
                                    Text(language.nativeName, style = SistaTheme.typography.bodyLarge)
                                    val localName = stringResource(languageName(language))
                                    if (localName != language.nativeName) {
                                        Text(localName, style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
                item(key = "note") {
                    Text(
                        stringResource(R.string.lang_note),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.xs),
                    )
                }
            }
        }
    }
}
