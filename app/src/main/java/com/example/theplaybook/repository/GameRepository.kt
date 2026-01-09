package com.example.theplaybook.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.theplaybook.data.local.GameDao
import com.example.theplaybook.data.local.entities.AchievementEntity
import com.example.theplaybook.data.local.entities.GameEntity
import com.example.theplaybook.data.remote.SteamApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date

// DataStore per salvare le preferenze
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class GameRepository(
    private val dao: GameDao,
    private val context: Context
) {

    companion object {
        // API service sarà inizializzato dall'Application Class
        lateinit var steamApiService: SteamApiService
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)

    // Preferenze utente
    private val steamIdKey = stringPreferencesKey("steam_id")
    private val steamNameKey = stringPreferencesKey("steam_name")

    // Salva Steam ID dell'utente
    suspend fun saveSteamUser(steamId: String, steamName: String) {
        context.dataStore.edit { preferences ->
            preferences[steamIdKey] = steamId
            preferences[steamNameKey] = steamName
        }
    }

    // Ottieni Steam ID salvato
    suspend fun getSavedSteamId(): String? {
        return context.dataStore.data.first()[steamIdKey]
    }

    suspend fun getSavedSteamName(): String? {
        return context.dataStore.data.first()[steamNameKey]
    }

    // Sincronizza dati da Steam
    suspend fun syncUserData(steamId: String): Result<DashboardData> {
        return try {
            withContext(Dispatchers.IO) {
                // 1. Ottieni profilo utente
                val profileResponse = steamApiService.getPlayerSummaries(steamId)
                val player = profileResponse.response.players.firstOrNull()

                if (player == null) {
                    return@withContext Result.failure(Exception("Profilo non trovato"))
                }

                // Salva nome utente
                saveSteamUser(steamId, player.personaName)

                // 2. Ottieni giochi posseduti
                val gamesResponse = steamApiService.getOwnedGames(steamId)
                val steamGames = gamesResponse.response.games

                // 3. Converti e salva giochi localmente
                val gameEntities = steamGames.map { steamGame ->
                    GameEntity(
                        appId = steamGame.appId,
                        name = steamGame.name,
                        playtimeForever = steamGame.playtimeForever,
                        playtime2Weeks = steamGame.playtime2Weeks,
                        iconUrl = steamGame.imgIconUrl,
                        logoUrl = steamGame.imgLogoUrl,
                        hasCommunityVisibleStats = steamGame.hasCommunityVisibleStats,
                        lastPlayed = steamGame.rtimeLastPlayed
                    )
                }

                dao.insertGames(gameEntities)

                // 4. Per ogni gioco con statistiche, ottieni achievement
                val gamesWithStats = gameEntities.filter { it.hasCommunityVisibleStats }

                gamesWithStats.take(5).forEach { game -> // Limita a 5 giochi per performance
                    try {
                        val achievementsResponse = steamApiService.getPlayerAchievements(
                            steamId,
                            game.appId
                        )

                        if (achievementsResponse.response.playerStats.success) {
                            val achievements = achievementsResponse.response.playerStats.achievements

                            achievements?.let { steamAchievements ->
                                val achievementEntities = steamAchievements.map { steamAchievement ->
                                    AchievementEntity(
                                        apiName = steamAchievement.apiName,
                                        gameId = game.appId,
                                        name = steamAchievement.name,
                                        description = steamAchievement.description,
                                        iconUrl = "", // Da implementare se necessario
                                        iconGrayUrl = "",
                                        achieved = steamAchievement.achieved == 1,
                                        unlockTime = steamAchievement.unlockTime,
                                        globalPercentage = 0f // Da implementare con API globale
                                    )
                                }

                                dao.insertAchievements(achievementEntities)
                            }
                        }
                    } catch (e: Exception) {
                        // Ignora errori per singolo gioco
                        e.printStackTrace()
                    }
                }

                // 5. Calcola statistiche dashboard
                val stats = dao.getDashboardStats()
                val recentGames = dao.getRecentGames(5).first()

                val dashboardData = DashboardData(
                    steamId = steamId,
                    steamName = player.personaName,
                    avatarUrl = player.avatarFull,
                    totalPlaytimeHours = (stats?.totalPlaytime ?: 0) / 60f,
                    totalGames = stats?.totalGames ?: 0,
                    completionRate = stats?.avgCompletion ?: 0f,
                    recentGames = recentGames,
                    lastUpdated = Date()
                )

                Result.success(dashboardData)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ottieni dati dalla cache locale
    suspend fun getCachedDashboardData(): DashboardData? {
        return try {
            val steamId = getSavedSteamId()
            val steamName = getSavedSteamName()

            if (steamId == null || steamName == null) {
                return null
            }

            val stats = dao.getDashboardStats()
            val recentGames = dao.getRecentGames(5).first()

            DashboardData(
                steamId = steamId,
                steamName = steamName,
                avatarUrl = "",
                totalPlaytimeHours = (stats?.totalPlaytime ?: 0) / 60f,
                totalGames = stats?.totalGames ?: 0,
                completionRate = stats?.avgCompletion ?: 0f,
                recentGames = recentGames,
                lastUpdated = Date()
            )
        } catch (e: Exception) {
            null
        }
    }

    // Flusso di giochi recenti
    fun getRecentGamesFlow(limit: Int = 5): Flow<List<GameEntity>> {
        return dao.getRecentGames(limit)
    }

    // Flusso di achievement per gioco
    fun getAchievementsForGame(gameId: Long): Flow<List<AchievementEntity>> {
        return dao.getAchievementsForGame(gameId)
    }

    // Ottieni achievement quasi completati
    fun getNearlyCompletedAchievements(gameId: Long): Flow<List<AchievementEntity>> {
        return dao.getNearlyCompletedAchievements(gameId)
    }

    // Cancella tutti i dati
    suspend fun clearAllData() {
        dao.deleteAllGames()
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// Modello per dati dashboard
data class DashboardData(
    val steamId: String,
    val steamName: String,
    val avatarUrl: String,
    val totalPlaytimeHours: Float,
    val totalGames: Int,
    val completionRate: Float,
    val recentGames: List<GameEntity>,
    val lastUpdated: Date
)