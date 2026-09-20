package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class GamificationProfile(
    @SerializedName("totalXp") val totalXp: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("levelTitle") val levelTitle: String,
    @SerializedName("xpToNextLevel") val xpToNextLevel: Int,
    @SerializedName("streakDays") val streakDays: Int,
    @SerializedName("longestStreak") val longestStreak: Int,
    @SerializedName("badgesEarned") val badgesEarned: Int,
    @SerializedName("monthlyRank") val monthlyRank: Int
)

data class GamificationProfileResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: GamificationProfile? = null
)

data class LeaderboardEntry(
    @SerializedName("rank") val rank: Int,
    @SerializedName("name") val name: String,
    @SerializedName("className") val className: String,
    @SerializedName("xp") val xp: Int,
    @SerializedName("isCurrentUser") val isCurrentUser: Boolean = false
)

data class LeaderboardResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeaderboardEntry> = emptyList()
)

data class Badge(
    @SerializedName("slug") val slug: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("iconEmoji") val iconEmoji: String,
    @SerializedName("category") val category: String,
    @SerializedName("xpReward") val xpReward: Int,
    @SerializedName("isEarned") val isEarned: Boolean
)

data class BadgeCollection(
    @SerializedName("earned") val earned: List<Badge> = emptyList(),
    @SerializedName("locked") val locked: List<Badge> = emptyList()
)

data class BadgeCollectionResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: BadgeCollection? = null
)

data class XpHistoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("activity") val activity: String,
    @SerializedName("xpEarned") val xpEarned: Int,
    @SerializedName("createdAt") val createdAt: String
)

data class XpHistoryResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<XpHistoryItem> = emptyList()
)
