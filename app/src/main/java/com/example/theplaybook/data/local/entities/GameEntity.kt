package com.example.theplaybook.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val appId: Long,
    val name: String,
    val playtimeForever: Int, // in minuti
    val playtime2Weeks: Int?,
    val iconUrl: String,
    val logoUrl: String,
    val hasCommunityVisibleStats: Boolean,
    val lastPlayed: Long?,
    val completionPercentage: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val apiName: String,
    val gameId: Long,
    val name: String,
    val description: String?,
    val iconUrl: String,
    val iconGrayUrl: String,
    val achieved: Boolean,
    val unlockTime: Long?,
    val globalPercentage: Float
)