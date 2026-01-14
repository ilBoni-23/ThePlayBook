package com.example.theplaybook.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class GameRepository(private val context: Context) {

    companion object {
        // Per Steam API (configura dopo)
        // lateinit var steamApiService: SteamApiService
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("theplaybook_prefs", Context.MODE_PRIVATE)
    }

    // Preferenze utente
    private val STEAM_ID_KEY = "steam_id"
    private val STEAM_NAME_KEY = "steam_name"
    private val USER_ID_KEY = "user_id"

    // Salva Steam ID
    fun saveSteamUser(steamId: String, steamName: String) {
        prefs.edit()
            .putString(STEAM_ID_KEY, steamId)
            .putString(STEAM_NAME_KEY, steamName)
            .apply()
    }

    // Ottieni Steam ID salvato
    fun getSavedSteamId(): String? {
        return prefs.getString(STEAM_ID_KEY, null)
    }

    fun getSavedSteamName(): String? {
        return prefs.getString(STEAM_NAME_KEY, null)
    }

    // Crea/ottieni user ID locale
    fun getOrCreateUserId(): String {
        var userId = prefs.getString(USER_ID_KEY, null)
        if (userId == null) {
            userId = "user_${System.currentTimeMillis()}"
            prefs.edit().putString(USER_ID_KEY, userId).apply()
        }
        return userId
    }

    // Dati demo per testing
    suspend fun getMockDashboardData(): DashboardData {
        return DashboardData(
            steamId = getSavedSteamId() ?: "76561197960287930",
            steamName = getSavedSteamName() ?: "Demo Player",
            avatarUrl = "",
            totalPlaytimeHours = 342.5f,
            totalGames = 67,
            completionRate = 78.3f,
            recentGames = getMockGames(),
            lastUpdated = Date()
        )
    }

    private fun getMockGames(): List<MockGame> {
        return listOf(
            MockGame("Counter-Strike 2", 1250, 65.0f),
            MockGame("Dota 2", 890, 42.0f),
            MockGame("Grand Theft Auto V", 156, 23.0f),
            MockGame("Cyberpunk 2077", 89, 45.0f),
            MockGame("Elden Ring", 234, 78.0f)
        )
    }

    // Cancella tutti i dati
    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    // Flusso di dati demo (per LiveData)
    fun getDashboardDataFlow(): Flow<DashboardData> = flow {
        emit(getMockDashboardData())
    }
}

// Modelli semplici (senza Room annotations)
data class DashboardData(
    val steamId: String,
    val steamName: String,
    val avatarUrl: String,
    val totalPlaytimeHours: Float,
    val totalGames: Int,
    val completionRate: Float,
    val recentGames: List<MockGame>,
    val lastUpdated: Date
)

data class MockGame(
    val name: String,
    val playtimeMinutes: Int,
    val completionPercentage: Float
)