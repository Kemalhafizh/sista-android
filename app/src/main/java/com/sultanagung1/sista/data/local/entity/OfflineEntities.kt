package com.sultanagung1.sista.data.local.entity

data class PendingActionItem(
    val id: String,
    val actionType: String, // "ATTENDANCE_CHECKIN", "MUTABAAH_LOG", "TEACHING_JOURNAL"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "PENDING" // "PENDING", "PROCESSING", "FAILED"
)

data class CacheMetadata(
    val key: String,
    val lastUpdated: Long,
    val expiryDurationMillis: Long
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() - lastUpdated > expiryDurationMillis
}
