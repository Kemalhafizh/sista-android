package com.sultanagung1.sista

import android.app.Application
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.repository.NotificationRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltAndroidApp
class SulaoneApplication : Application() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var notificationRepository: NotificationRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 1. Catat waktu awal Cold Start
        com.sultanagung1.sista.core.telemetry.AppStartupTracker.recordAppStart()

        // 2. Inisialisasi SulaoneTelemetryHub & Global Crash Handler
        com.sultanagung1.sista.core.telemetry.SulaoneTelemetryHub.instance.initialize(this)

        // 3. Real FCM device registration — fires on app start when a
        // session already exists, and again on every fresh login (both are
        // just authTokenFlow transitioning to a non-null value). Token
        // rotation after that is handled separately in
        // SulaoneFirebaseMessagingService.onNewToken().
        registerDeviceTokenWhenAuthenticated()
    }

    private fun registerDeviceTokenWhenAuthenticated() {
        appScope.launch {
            sessionManager.authTokenFlow
                .distinctUntilChanged()
                .filterNotNull()
                .collect {
                    try {
                        val token = FirebaseMessaging.getInstance().token.await()
                        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                            ?: "unknown-device"
                        notificationRepository.registerDeviceToken(deviceId, token).collect { }
                    } catch (e: Exception) {
                        // Best-effort — a missed registration here just means push
                        // notifications won't reach this device until the next
                        // successful attempt (next login, or FCM's own token
                        // rotation via SulaoneFirebaseMessagingService.onNewToken).
                    }
                }
        }
    }
}
