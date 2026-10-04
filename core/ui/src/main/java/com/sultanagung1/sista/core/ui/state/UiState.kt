package com.sultanagung1.sista.core.ui.state

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import androidx.compose.foundation.layout.padding

/**
 * The single contract for anything a screen loads. Every rebuilt screen
 * exposes its data as one of these, so loading, empty, error and "showing
 * saved data" look and behave the same everywhere.
 */
@Immutable
sealed interface UiState<out T> {
    /** First load, nothing to show yet. */
    data object Loading : UiState<Nothing>

    /** Loaded, and there is nothing (e.g. no lessons today). */
    data class Empty(val title: String, val body: String? = null) : UiState<Nothing>

    /** Could not load and there is nothing cached. [message] is user-facing. */
    data class Error(val message: String) : UiState<Nothing>

    /**
     * Data to show. [refreshing] while a reload runs in the background;
     * [notice] when what is shown may be stale (offline, cached copy).
     */
    data class Content<T>(
        val data: T,
        val refreshing: Boolean = false,
        val notice: String? = null,
    ) : UiState<T>
}

/** Renders the standard loading / empty / error states and delegates [content]. */
@Composable
fun <T> StatefulContent(
    state: UiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorTitle: String = stringResource(R.string.core_load_failed),
    loading: @Composable () -> Unit = { SkeletonList() },
    content: @Composable (T) -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        when (state) {
            UiState.Loading -> loading()
            is UiState.Empty -> EmptyState(title = state.title, body = state.body)
            is UiState.Error -> ErrorState(title = errorTitle, body = state.message, onRetry = onRetry)
            is UiState.Content -> {
                if (state.notice != null) {
                    InlineBanner(
                        message = state.notice,
                        tone = StatusTone.Warning,
                        actionLabel = stringResource(R.string.core_reload),
                        onAction = onRetry,
                        modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                    )
                }
                content(state.data)
            }
        }
    }
}
