package com.sultanagung1.sista.core.sync

enum class ConflictResolutionStrategy {
    SERVER_WINS,
    CLIENT_WINS,
    TIMESTAMP_LATEST
}

data class SyncConflict<T>(
    val entityId: String,
    val entityType: String,
    val localVersion: T,
    val remoteVersion: T,
    val localTimestamp: Long,
    val remoteTimestamp: Long
)

object ConflictResolver {
    fun <T> resolve(
        conflict: SyncConflict<T>,
        strategy: ConflictResolutionStrategy = ConflictResolutionStrategy.SERVER_WINS
    ): T {
        return when (strategy) {
            ConflictResolutionStrategy.SERVER_WINS -> conflict.remoteVersion
            ConflictResolutionStrategy.CLIENT_WINS -> conflict.localVersion
            ConflictResolutionStrategy.TIMESTAMP_LATEST -> {
                if (conflict.localTimestamp >= conflict.remoteTimestamp) {
                    conflict.localVersion
                } else {
                    conflict.remoteVersion
                }
            }
        }
    }
}
