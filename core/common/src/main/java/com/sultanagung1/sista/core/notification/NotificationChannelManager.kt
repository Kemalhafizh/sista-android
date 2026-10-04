package com.sultanagung1.sista.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.sultanagung1.sista.core.common.R
import com.sultanagung1.sista.data.model.NotificationChannelType

class NotificationChannelManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelType.values().forEach { channelType ->
                val importance = when (channelType.importance) {
                    5 -> NotificationManager.IMPORTANCE_HIGH
                    4 -> NotificationManager.IMPORTANCE_HIGH
                    3 -> NotificationManager.IMPORTANCE_DEFAULT
                    else -> NotificationManager.IMPORTANCE_LOW
                }

                // Named in the app's language; creating it again renames it, so
                // Android's settings follow a language change on the next start.
                val channel = NotificationChannel(
                    channelType.channelId,
                    context.getString(channelType.labelRes()),
                    importance
                ).apply {
                    description = context.getString(channelType.descriptionRes())
                    enableVibration(channelType.importance >= 4)
                    enableLights(true)
                }

                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun dispatchLocalNotification(
        notificationId: Int,
        channelType: NotificationChannelType,
        title: String,
        message: String,
        deepLinkRoute: String? = null
    ) {
        // Resolved via the launcher intent rather than a direct MainActivity
        // reference — core/notification can't depend on :app (where
        // MainActivity lives) without creating a module cycle, since :app
        // depends on every core/feature module already.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val intent = (launchIntent ?: Intent()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!deepLinkRoute.isNullOrEmpty()) {
                data = Uri.parse("sulaone://$deepLinkRoute")
                putExtra("DEEP_LINK_ROUTE", deepLinkRoute)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelType.channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(
                when (channelType.importance) {
                    5 -> NotificationCompat.PRIORITY_MAX
                    4 -> NotificationCompat.PRIORITY_HIGH
                    3 -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_LOW
                }
            )

        notificationManager.notify(notificationId, builder.build())
    }
}

/** The channel's name and description in the app's language (res/values*, channel_*). */
@StringRes
fun NotificationChannelType.labelRes(): Int = when (this) {
    NotificationChannelType.ATTENDANCE -> R.string.channel_attendance
    NotificationChannelType.ACADEMIC -> R.string.channel_academic
    NotificationChannelType.FINANCE -> R.string.channel_finance
    NotificationChannelType.EMERGENCY -> R.string.channel_emergency
    NotificationChannelType.GENERAL -> R.string.channel_general
}

@StringRes
fun NotificationChannelType.descriptionRes(): Int = when (this) {
    NotificationChannelType.ATTENDANCE -> R.string.channel_attendance_desc
    NotificationChannelType.ACADEMIC -> R.string.channel_academic_desc
    NotificationChannelType.FINANCE -> R.string.channel_finance_desc
    NotificationChannelType.EMERGENCY -> R.string.channel_emergency_desc
    NotificationChannelType.GENERAL -> R.string.channel_general_desc
}
