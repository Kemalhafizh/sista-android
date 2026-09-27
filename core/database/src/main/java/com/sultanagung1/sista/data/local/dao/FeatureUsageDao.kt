package com.sultanagung1.sista.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sultanagung1.sista.data.local.entity.FeatureUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FeatureUsageDao {

    @Query("SELECT * FROM feature_usage WHERE user_id = :userId")
    abstract fun observeForUser(userId: String): Flow<List<FeatureUsageEntity>>

    @Query(
        "UPDATE feature_usage SET tap_count = tap_count + 1, last_used_at = :now " +
            "WHERE user_id = :userId AND feature_key = :featureKey"
    )
    abstract suspend fun incrementExisting(userId: String, featureKey: String, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(entity: FeatureUsageEntity): Long

    /**
     * Update-then-insert instead of SQLite's `ON CONFLICT DO UPDATE` upsert:
     * that syntax needs SQLite 3.24, which Android only ships from API 30,
     * and minSdk is 26.
     */
    @Transaction
    open suspend fun recordTap(userId: String, featureKey: String, now: Long) {
        if (incrementExisting(userId, featureKey, now) == 0) {
            insertIfAbsent(FeatureUsageEntity(userId, featureKey, tapCount = 1, lastUsedAt = now))
        }
    }

    @Query("DELETE FROM feature_usage WHERE user_id = :userId")
    abstract suspend fun clearForUser(userId: String)
}
