package com.sultanagung1.sista.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FeatureTile
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.AppFeature
import com.sultanagung1.sista.data.model.Capabilities
import com.sultanagung1.sista.data.model.CapabilityState
import com.sultanagung1.sista.data.model.FeatureGroup
import com.sultanagung1.sista.ui.navigation.FeatureCatalog

/**
 * "Layanan": every feature the account has, grouped the way the server
 * groups them. The layout is the same for every role; only the tiles differ,
 * because they come from the account's capability list. A feature with no
 * screen in this version of the app is not listed.
 */
@Composable
fun ServicesHubScreen(
    state: CapabilityState,
    onOpen: (route: String) -> Unit,
    onRetry: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = "Layanan", large = true, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(padding)
            when (state) {
                CapabilityState.Loading -> SkeletonList(contentModifier.padding(Spacing.screen))
                CapabilityState.SignedOut -> EmptyState(
                    title = "Anda belum masuk",
                    body = "Masuk untuk melihat layanan sekolah Anda.",
                    modifier = contentModifier,
                )
                is CapabilityState.Failed -> ErrorState(
                    title = "Layanan belum termuat",
                    body = state.message,
                    onRetry = onRetry,
                    modifier = contentModifier,
                )
                is CapabilityState.Ready -> ServicesGrid(
                    capabilities = state.capabilities,
                    fromCache = state.fromCache,
                    onOpen = onOpen,
                    onRetry = onRetry,
                    modifier = contentModifier,
                )
            }
        }
    }
}

@Composable
private fun ServicesGrid(
    capabilities: Capabilities,
    fromCache: Boolean,
    onOpen: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val sections: List<Pair<FeatureGroup, List<AppFeature>>> = remember(capabilities, query) {
        FeatureCatalog.menuFeatures(capabilities, query)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 152.dp),
        modifier = modifier.testTag("services_grid"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.md),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.md),
    ) {
        if (fromCache) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                InlineBanner(
                    message = "Tidak terhubung ke server. Ini daftar layanan yang tersimpan terakhir.",
                    tone = StatusTone.Warning,
                    actionLabel = "Muat ulang",
                    onAction = onRetry,
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SistaTextField(
                value = query,
                onValueChange = { query = it },
                label = "Cari layanan",
                leadingIcon = Icons.Outlined.Search,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (sections.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    title = if (query.isBlank()) "Belum ada layanan" else "Tidak ditemukan",
                    body = if (query.isBlank()) {
                        "Akun Anda belum mendapat layanan di aplikasi. Hubungi admin sekolah."
                    } else {
                        "Tidak ada layanan bernama \"$query\"."
                    },
                    icon = Icons.Outlined.SearchOff,
                )
            }
        }
        sections.forEach { (group, features) ->
            item(key = "group-${group.key}", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = group.title, modifier = Modifier.padding(top = Spacing.sm))
            }
            items(features, key = { it.key }) { feature ->
                val hints = feature.hardware.mapNotNull(FeatureIcons::hint)
                val hintText = feature.hardware.mapNotNull(FeatureIcons::hintLabel)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(prefix = "Memakai ")
                FeatureTile(
                    title = feature.title,
                    icon = FeatureIcons.of(feature.key),
                    hints = hints,
                    hintDescription = hintText,
                    onClick = { FeatureCatalog.ENTRY[feature.key]?.let(onOpen) },
                )
            }
        }
    }
}
