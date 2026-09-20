package com.sultanagung1.sista.core.display

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.WindowManager
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Modes of display refresh rate supported by SULAONE.
 */
enum class RefreshRateMode(val title: String, val description: String) {
    ADAPTIVE_SMOOTH(
        "Adaptif / Maksimal (60Hz / 90Hz / 120Hz / LTPO)",
        "Otomatis mengikuti laju penyegaran layar HP Anda untuk scrolling sangat mulus"
    ),
    POWER_SAVER_60HZ(
        "Hemat Daya (Terkunci 60Hz)",
        "Mengunci laju penyegaran pada 60Hz standar untuk menghemat daya baterai"
    ),
    SYSTEM_DEFAULT(
        "Default Sistem Android",
        "Mengikuti pengaturan umum sistem operasi perangkat Anda"
    )
}

/**
 * Diagnostic capabilities of the connected device's display hardware.
 */
data class DisplayCapabilities(
    val currentRefreshRate: Float,
    val maxSupportedRefreshRate: Float,
    val supportedRefreshRates: List<Float>,
    val isHighRefreshRateSupported: Boolean,
    val isLtpoSupported: Boolean,
    val summaryText: String
)

/**
 * Enterprise Adaptive Refresh Rate Engine.
 *
 * Directly unlocks high refresh rates (90Hz / 120Hz / 144Hz) and LTPO variable
 * refresh rate on devices like Xiaomi, POCO, Samsung, and OnePlus, eliminating
 * the 60Hz lock and stuttering/jank on Jetpack Compose screens.
 */
object AdaptiveRefreshRateManager {

    /**
     * Detects display hardware capabilities of the current device.
     */
    fun detectCapabilities(context: Context): DisplayCapabilities {
        val display = getDisplay(context)
        if (display == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return DisplayCapabilities(
                currentRefreshRate = 60f,
                maxSupportedRefreshRate = 60f,
                supportedRefreshRates = listOf(60f),
                isHighRefreshRateSupported = false,
                isLtpoSupported = false,
                summaryText = "60Hz Standar"
            )
        }

        val currentRate = display.mode.refreshRate
        val supportedModes = display.supportedModes
        val distinctRates = supportedModes
            .map { (it.refreshRate * 10f).roundToInt() / 10f }
            .distinct()
            .sorted()

        val maxRate = distinctRates.maxOrNull() ?: currentRate
        val isHighRate = maxRate > 60.5f

        // LTPO detection: devices with 3+ distinct refresh rate steps (e.g. 10/30/60/90/120)
        // or dynamic variable refresh rate capabilities
        val isLtpo = distinctRates.size >= 3 && isHighRate

        val summary = when {
            isLtpo -> "${maxRate.roundToInt()}Hz Adaptif (LTPO Didukung)"
            isHighRate -> "${maxRate.roundToInt()}Hz Smooth Display"
            else -> "60Hz Standar"
        }

        return DisplayCapabilities(
            currentRefreshRate = currentRate,
            maxSupportedRefreshRate = maxRate,
            supportedRefreshRates = distinctRates,
            isHighRefreshRateSupported = isHighRate,
            isLtpoSupported = isLtpo,
            summaryText = summary
        )
    }

    /**
     * Applies the specified refresh rate mode directly to an Activity's Window.
     */
    fun applyToActivity(activity: Activity, mode: RefreshRateMode = RefreshRateMode.ADAPTIVE_SMOOTH) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val window = activity.window ?: return
        val display = getDisplay(activity) ?: return
        val currentMode = display.mode
        val supportedModes = display.supportedModes
        val layoutParams = window.attributes

        when (mode) {
            RefreshRateMode.ADAPTIVE_SMOOTH -> {
                // Find mode with matching resolution and highest refresh rate
                val matchingModes = supportedModes.filter {
                    it.physicalWidth == currentMode.physicalWidth &&
                    it.physicalHeight == currentMode.physicalHeight
                }
                val bestMode = matchingModes.maxByOrNull { it.refreshRate }
                    ?: supportedModes.maxByOrNull { it.refreshRate }

                if (bestMode != null) {
                    layoutParams.preferredDisplayModeId = bestMode.modeId
                    layoutParams.preferredRefreshRate = bestMode.refreshRate
                }

                // 0f allows LTPO display controller to step down to 1Hz-30Hz when static,
                // and ramp up to 90Hz/120Hz during user scrolls and gestures.
                val maxRate = bestMode?.refreshRate ?: currentMode.refreshRate
                trySetMinMaxRefreshRate(layoutParams, minRate = 0f, maxRate = maxRate)
            }

            RefreshRateMode.POWER_SAVER_60HZ -> {
                val matchingModes = supportedModes.filter {
                    it.physicalWidth == currentMode.physicalWidth &&
                    it.physicalHeight == currentMode.physicalHeight
                }
                // Find mode closest to 60Hz
                val mode60 = matchingModes.minByOrNull { kotlin.math.abs(it.refreshRate - 60f) }
                    ?: supportedModes.minByOrNull { kotlin.math.abs(it.refreshRate - 60f) }

                if (mode60 != null) {
                    layoutParams.preferredDisplayModeId = mode60.modeId
                    layoutParams.preferredRefreshRate = mode60.refreshRate
                }

                trySetMinMaxRefreshRate(layoutParams, minRate = 0f, maxRate = 60f)
            }

            RefreshRateMode.SYSTEM_DEFAULT -> {
                layoutParams.preferredDisplayModeId = 0
                layoutParams.preferredRefreshRate = 0f
                trySetMinMaxRefreshRate(layoutParams, minRate = 0f, maxRate = 0f)
            }
        }

        window.attributes = layoutParams
    }

    /**
     * Observes stored refresh rate mode from SessionManager.
     */
    fun getStoredModeFlow(sessionManager: SessionManager): Flow<RefreshRateMode> {
        return sessionManager.refreshRateModeFlow.map { name ->
            when (name) {
                RefreshRateMode.POWER_SAVER_60HZ.name -> RefreshRateMode.POWER_SAVER_60HZ
                RefreshRateMode.SYSTEM_DEFAULT.name -> RefreshRateMode.SYSTEM_DEFAULT
                else -> RefreshRateMode.ADAPTIVE_SMOOTH // Default is adaptive high refresh rate
            }
        }
    }

    /**
     * Persists user preference and applies it.
     */
    fun saveMode(
        sessionManager: SessionManager,
        mode: RefreshRateMode,
        activity: Activity? = null,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        coroutineScope.launch {
            sessionManager.saveRefreshRateMode(mode.name)
        }
        activity?.let {
            applyToActivity(it, mode)
        }
    }

    private fun trySetMinMaxRefreshRate(layoutParams: WindowManager.LayoutParams, minRate: Float, maxRate: Float) {
        try {
            val maxField = layoutParams.javaClass.getField("preferredMaxDisplayRefreshRate")
            maxField.setFloat(layoutParams, maxRate)
        } catch (_: Throwable) {}

        try {
            val minField = layoutParams.javaClass.getField("preferredMinDisplayRefreshRate")
            minField.setFloat(layoutParams, minRate)
        } catch (_: Throwable) {}
    }

    private fun getDisplay(context: Context): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (context is Activity) {
                    context.display
                } else {
                    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                    displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
                }
            } catch (_: Exception) {
                val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            }
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
        }
    }
}
