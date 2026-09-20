package com.sultanagung1.sista

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SulaoneApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 1. Catat waktu awal Cold Start
        com.sultanagung1.sista.core.telemetry.AppStartupTracker.recordAppStart()

        // 2. Inisialisasi SulaoneTelemetryHub & Global Crash Handler
        com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.initialize(this)
    }
}
