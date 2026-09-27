package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.GamificationApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GamificationRepository(private val apiService: GamificationApiService) {

    fun getProfile(): Flow<NetworkResult<GamificationProfile?>> = flow<NetworkResult<GamificationProfile?>> {
        emit(NetworkResult.Loading)
        try {
            val res = apiService.getProfile()
            if (res.isSuccessful) {
                emit(NetworkResult.Success(res.body()?.data))
            } else {
                emit(NetworkResult.Error(res.message().ifBlank { "Gagal memuat profil gamifikasi" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi gamifikasi"))
        }
    }.flowOn(Dispatchers.IO)

    fun getLeaderboard(scope: String? = null, period: String? = null): Flow<NetworkResult<List<LeaderboardEntry>>> = flow<NetworkResult<List<LeaderboardEntry>>> {
        emit(NetworkResult.Loading)
        try {
            val res = apiService.getLeaderboard(scope, period)
            if (res.isSuccessful) {
                val list: List<LeaderboardEntry> = res.body()?.data ?: emptyList()
                emit(NetworkResult.Success(list))
            } else {
                emit(NetworkResult.Error(res.message().ifBlank { "Gagal memuat papan peringkat" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi leaderboard"))
        }
    }.flowOn(Dispatchers.IO)

    fun getBadges(): Flow<NetworkResult<BadgeCollection?>> = flow<NetworkResult<BadgeCollection?>> {
        emit(NetworkResult.Loading)
        try {
            val res = apiService.getBadges()
            if (res.isSuccessful) {
                emit(NetworkResult.Success(res.body()?.data))
            } else {
                emit(NetworkResult.Error(res.message().ifBlank { "Gagal memuat lencana prestasi" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi lencana"))
        }
    }.flowOn(Dispatchers.IO)

    fun getHistory(): Flow<NetworkResult<List<XpHistoryItem>>> = flow<NetworkResult<List<XpHistoryItem>>> {
        emit(NetworkResult.Loading)
        try {
            val res = apiService.getHistory()
            if (res.isSuccessful) {
                val list: List<XpHistoryItem> = res.body()?.data ?: emptyList()
                emit(NetworkResult.Success(list))
            } else {
                emit(NetworkResult.Error(res.message().ifBlank { "Gagal memuat riwayat poin XP" }))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi riwayat XP"))
        }
    }.flowOn(Dispatchers.IO)
}
