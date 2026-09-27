package com.sultanagung1.sista.core.security

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class LivenessState(
    val isActive: Boolean = false,
    val totalChecksCompleted: Int = 0,
    val lastCheckTime: Long = 0L,
    val isCameraPulseActive: Boolean = false
)

class RandomLivenessProctor(private val context: Context) {

    private val _livenessState = MutableStateFlow(LivenessState())
    val livenessState: StateFlow<LivenessState> = _livenessState.asStateFlow()

    private var proctorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Start periodic randomized liveness monitoring during CBT exam.
     * Triggers random audit pulses between 8 to 15 minutes.
     */
    fun startMonitoring(onAuditPulse: (() -> Unit)? = null) {
        if (_livenessState.value.isActive) return

        _livenessState.value = _livenessState.value.copy(isActive = true)
        Log.d("RandomLivenessProctor", "Randomized liveness audit started.")

        proctorJob = scope.launch {
            while (isActive) {
                // Wait randomized interval between 8 and 14 minutes (in ms)
                // For testability, lower bounds are respected in production
                val intervalMs = Random.nextLong(480_000L, 840_000L)
                delay(intervalMs)

                if (!isActive) break

                // Trigger pulse
                _livenessState.value = _livenessState.value.copy(isCameraPulseActive = true)
                onAuditPulse?.invoke()
                delay(2000L) // Pulse indicator for 2 seconds

                val count = _livenessState.value.totalChecksCompleted + 1
                _livenessState.value = _livenessState.value.copy(
                    isCameraPulseActive = false,
                    totalChecksCompleted = count,
                    lastCheckTime = System.currentTimeMillis()
                )
                Log.d("RandomLivenessProctor", "Liveness pulse completed. Total: $count")
            }
        }
    }

    /**
     * Stop liveness monitoring when exam concludes.
     */
    fun stopMonitoring() {
        proctorJob?.cancel()
        proctorJob = null
        _livenessState.value = _livenessState.value.copy(
            isActive = false,
            isCameraPulseActive = false
        )
        Log.d("RandomLivenessProctor", "Randomized liveness audit stopped.")
    }
}
