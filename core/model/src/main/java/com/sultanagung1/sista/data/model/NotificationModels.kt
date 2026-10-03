package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for Push Notifications and Channel Routing
 */
enum class NotificationChannelType(
    val channelId: String,
    val channelName: String,
    val channelDesc: String,
    val importance: Int
) {
    ATTENDANCE("attendance_alerts", "Presensi & Kehadiran", "Notifikasi kehadiran siswa tiba di gerbang sekolah", 4),
    ACADEMIC("academic_updates", "Akademik & Rapor", "Pembaruan nilai rapor, jadwal asesmen, dan tugas", 3),
    FINANCE("financial_reminders", "Keuangan & SPP", "Pengingat tagihan SPP dan status pembayaran", 4),
    EMERGENCY("emergency_broadcast", "Darurat & Krisis", "Siaran darurat krisis dan keselamatan kampus", 5),
    GENERAL("general_info", "Informasi Umum", "Pengumuman kegiatan yayasan dan berita sekolah", 2)
}

data class DeviceTokenRegisterRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("platform") val platform: String = "android",
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("device_name") val deviceName: String = "Android Device",
    @SerializedName("app_version") val appVersion: String = "2.0"
)

/** One notification from `notifications`, this account's own. */
data class NotificationItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String = "",
    /** One of [NotificationChannelType] channel ids. */
    @SerializedName("channel") val channel: String = "general_info",
    /** An app route ("billing", "discipline", "uks_visit") or null. */
    @SerializedName("deep_link_route") val deepLinkRoute: String? = null,
    /** ISO-8601. */
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("is_read") val isRead: Boolean = false,
)

data class UnreadCount(@SerializedName("count") val count: Int = 0)
