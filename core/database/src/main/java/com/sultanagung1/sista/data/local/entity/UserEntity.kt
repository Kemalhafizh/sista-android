package com.sultanagung1.sista.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sultanagung1.sista.data.model.UserProfile

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val uuid: String?,
    val name: String,
    val email: String,
    val role: String,
    val phoneNumber: String?,
    val nisn: String?,
    val nip: String?,
    val classroom: String?,
    val avatarUrl: String?
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            uuid = uuid,
            name = name,
            email = email,
            role = role,
            phoneNumber = phoneNumber,
            nisn = nisn,
            nip = nip,
            classroom = classroom,
            avatarUrl = avatarUrl
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile): UserEntity {
            return UserEntity(
                id = profile.id ?: 1L,
                uuid = profile.uuid,
                name = profile.name,
                email = profile.email,
                role = profile.role,
                phoneNumber = profile.phoneNumber,
                nisn = profile.nisn,
                nip = profile.nip,
                classroom = profile.classroom,
                avatarUrl = profile.avatarUrl
            )
        }
    }
}
