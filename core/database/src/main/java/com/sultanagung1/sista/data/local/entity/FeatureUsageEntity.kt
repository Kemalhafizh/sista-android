package com.sultanagung1.sista.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * FASE 76.4: how often this account opened a feature from Home, on this device.
 * Keyed by user as well as feature, so a phone shared between a parent's and a
 * child's account keeps two separate orderings. Nothing here leaves the device.
 */
@Entity(tableName = "feature_usage", primaryKeys = ["user_id", "feature_key"])
data class FeatureUsageEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "feature_key") val featureKey: String,
    @ColumnInfo(name = "tap_count") val tapCount: Int,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Long
)
