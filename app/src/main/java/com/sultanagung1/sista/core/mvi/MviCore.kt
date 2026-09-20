package com.sultanagung1.sista.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Marker interface for all MVI UI states.
 * States must be immutable data classes representing the complete screen state.
 */
interface UiState

/**
 * Marker interface for all MVI UI events / user intents.
 * Events must be sealed classes or sealed interfaces representing user actions or system triggers.
 */
interface UiEvent

/**
 * Marker interface for one-shot UI side effects (e.g. Navigation, Toast, Snackbar, Haptics).
 */
interface UiEffect

/**
 * Base MVI ViewModel ensuring unidirectional data flow (UDF):
 * UI -> [UiEvent] -> ViewModel -> [UiState] -> UI
 *                             `-> [UiEffect] -> UI (One-shot)
 */
abstract class MviViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<F>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected val currentState: S
        get() = _uiState.value

    protected fun setState(reducer: S.() -> S) {
        _uiState.value = _uiState.value.reducer()
    }

    protected suspend fun emitEffect(builder: () -> F) {
        _effect.send(builder())
    }

    /**
     * Single entry-point for all UI interactions and intents.
     */
    abstract fun onEvent(event: E)
}
