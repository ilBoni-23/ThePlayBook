package com.example.theplaybook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.theplaybook.data.local.entities.AchievementEntity
import com.example.theplaybook.data.local.entities.GameEntity

@Database(
    entities = [GameEntity::class, AchievementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        const val DATABASE_NAME = "playbook_database"
    }
}