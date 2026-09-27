package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.local.dao.FeatureUsageDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * FASE 76.4: local, per-account tap counts behind Home's "Sering Dipakai"
 * ordering. Feature modules reach the Room table only through this class
 * (FASE 73.2: :core:database stays behind :core:data).
 */
class FeatureUsageRepository(
    private val dao: FeatureUsageDao,
    private val sessionManager: SessionManager
) {

    /** feature key → tap count for whoever is logged in; empty when nobody is. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun usageCounts(): Flow<Map<String, Int>> =
        sessionManager.userIdFlow.flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf(emptyMap())
            } else {
                dao.observeForUser(userId).map { rows -> rows.associate { it.featureKey to it.tapCount } }
            }
        }

    suspend fun recordTap(featureKey: String) {
        val userId = sessionManager.userIdFlow.first()
        if (userId.isNullOrBlank() || featureKey.isBlank()) return
        dao.recordTap(userId, featureKey, System.currentTimeMillis())
    }

    /** "Atur ulang urutan": forget this account's counts on this device. */
    suspend fun reset() {
        val userId = sessionManager.userIdFlow.first()
        if (userId.isNullOrBlank()) return
        dao.clearForUser(userId)
    }
}
