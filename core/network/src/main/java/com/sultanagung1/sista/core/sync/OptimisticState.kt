package com.sultanagung1.sista.core.sync

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FASE 61.4: Optimistic UI State Rollback Helper.
 *
 * Provides atomic snapshot and rollback semantics for optimistic mutations.
 * If the mutating network or database operation fails (4xx/5xx/timeout/exception),
 * the state is atomically restored to its previous value and the rollback hook is executed.
 */

/**
 * Encapsulates a snapshot of a [MutableStateFlow] for explicit rollback operations.
 */
class OptimisticSnapshot<T>(
    private val stateFlow: MutableStateFlow<T>,
    val originalState: T
) {
    /**
     * Atomically restores the StateFlow to its original snapshot value.
     */
    fun rollback() {
        stateFlow.value = originalState
    }
}

/**
 * Captures an [OptimisticSnapshot] of the current [MutableStateFlow] value.
 */
fun <T> MutableStateFlow<T>.createSnapshot(): OptimisticSnapshot<T> {
    return OptimisticSnapshot(this, this.value)
}

/**
 * Executes an optimistic state transition followed by a suspending mutation block.
 *
 * 1. Takes an atomic snapshot of current value.
 * 2. Applies [optimisticTransform] immediately so the UI reflects the change with 0ms latency.
 * 3. Executes the suspending [mutation] block.
 * 4. If [mutation] throws any [Throwable], atomically restores the original value,
 *    invokes [onRollback], and returns [Result.failure].
 * 5. If [mutation] succeeds, returns [Result.success].
 */
suspend fun <T> MutableStateFlow<T>.optimisticUpdate(
    optimisticTransform: (T) -> T,
    onRollback: ((originalState: T, error: Throwable) -> Unit)? = null,
    mutation: suspend () -> Unit
): Result<Unit> {
    val snapshot = this.value
    this.value = optimisticTransform(snapshot)
    return try {
        mutation()
        Result.success(Unit)
    } catch (e: Throwable) {
        this.value = snapshot
        onRollback?.invoke(snapshot, e)
        Result.failure(e)
    }
}
