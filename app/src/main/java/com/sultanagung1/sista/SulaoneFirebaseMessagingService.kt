package com.sultanagung1.sista

import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.notification.NotificationChannelManager
import com.sultanagung1.sista.data.model.NotificationChannelType
import com.sultanagung1.sista.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Real FCM entry point — until now the app had a backend (real
 * PushNotificationService/UserDeviceToken), a Retrofit call
 * (NotificationApiService.registerDevice), and a Repository method
 * (registerDeviceToken), but no FCM SDK anywhere and nothing ever actually
 * called that repository method. This closes the loop: token rotation gets
 * re-registered here, the initial token-after-login case is handled by
 * SulaoneApplication observing SessionManager's auth state.
 */
@AndroidEntryPoint
class SulaoneFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationRepository: NotificationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown-device"
            notificationRepository.registerDeviceToken(deviceId, token).collect { result ->
                when (result) {
                    is NetworkResult.Error -> android.util.Log.w(
                        "SulaoneFcmService",
                        "Gagal mendaftarkan token FCM baru: ${result.message}"
                    )
                    else -> Unit
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val channelManager = NotificationChannelManager(applicationContext)
        val title = message.notification?.title ?: message.data["title"] ?: "Notifikasi Sulaone"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val channelType = NotificationChannelType.values()
            .firstOrNull { it.channelId == message.data["channel"] }
            ?: NotificationChannelType.GENERAL
        val deepLinkRoute = message.data["deep_link_route"]

        channelManager.createNotificationChannels()
        channelManager.dispatchLocalNotification(
            notificationId = System.currentTimeMillis().toInt(),
            channelType = channelType,
            title = title,
            message = body,
            deepLinkRoute = deepLinkRoute
        )
    }
}
