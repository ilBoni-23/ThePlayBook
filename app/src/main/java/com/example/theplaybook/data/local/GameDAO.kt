package com.example.theplaybook.data.local

import androidx.room.*
import com.example.theplaybook.data.local.entities.AchievementEntity
import com.example.theplaybook.data.local.entities.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    // Operazioni per i giochi
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Query("SELECT * FROM games ORDER BY lastPlayed DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentGames(limit: Int = 10): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE appId = :appId")
    suspend fun getGameById(appId: Long): GameEntity?

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()

    // Operazioni per gli achievement
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM achievements WHERE gameId = :gameId")
    fun getAchievementsForGame(gameId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE gameId = :gameId AND achieved = 0 ORDER BY globalPercentage DESC")
    fun getNearlyCompletedAchievements(gameId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT SUM(playtimeForever) FROM games")
    suspend fun getTotalPlaytime(): Int?

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getTotalGames(): Int

    // Statistiche aggregate
    @Query("""
        SELECT 
            COALESCE(SUM(playtimeForever), 0) as totalPlaytime,
            COUNT(*) as totalGames,
            AVG(completionPercentage) as avgCompletion
        FROM games
    """)
    suspend fun getDashboardStats(): DashboardStatsEntity?
}

data class DashboardStatsEntity(
    val totalPlaytime: Int,
    val totalGames: Int,
    val avgCompletion: Float?
)