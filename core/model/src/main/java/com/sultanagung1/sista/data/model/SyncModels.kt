package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * FASE 61: Sync Engine Hardening Data Models.
 */
data class TombstoneItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("is_deleted") val isDeleted: Boolean = true,
    @SerializedName("deleted_at") val deletedAt: String? = null
)

data class DeltaSyncResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("sync_timestamp") val syncTimestamp: Long = 0L,
    @SerializedName("delta_count") val deltaCount: Int = 0,
    @SerializedName("tombstones") val tombstones: List<TombstoneItem> = emptyList()
)

data class CatchUpEventItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: Map<String, Any>? = null
)

data class CatchUpResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("last_event_id") val lastEventId: String = "0",
    @SerializedName("events") val events: List<CatchUpEventItem> = emptyList()
)
