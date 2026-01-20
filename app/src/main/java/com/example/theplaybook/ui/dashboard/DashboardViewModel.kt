package com.example.theplaybook.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theplaybook.data.RepositoryFactory
import com.example.theplaybook.data.remote.SteamApiService
import com.example.theplaybook.data.remote.models.*
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository: SteamApiService = RepositoryFactory.createSteamRepository()

    private val _uiState = MutableLiveData<DashboardUiState>(DashboardUiState.Loading)
    val uiState: LiveData<DashboardUiState> = _uiState

    private val _isMockMode = MutableLiveData(RepositoryFactory.isMockMode())
    val isMockMode: LiveData<Boolean> = _isMockMode

    fun loadDashboardData(steamId: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            try {
                // 1. Carica profilo
                val profileResponse = repository.getPlayerSummaries(steamId)
                val player = profileResponse.response.players.first()

                // 2. Carica giochi
                val gamesResponse = repository.getOwnedGames(steamId)
                val games = gamesResponse.response.games

                // 3. Calcola statistiche
                val totalPlaytimeHours = games.sumOf { it.playtimeForever } / 60f
                val recentGames = games
                    .filter { it.rtimeLastPlayed != null }
                    .sortedByDescending { it.rtimeLastPlayed }
                    .take(5)

                // 4. Per i primi 3 giochi, carica achievement
                val nearlyCompleted = mutableListOf<SteamAchievement>()
                games.take(3).forEach { game ->
                    try {
                        val achievementsResponse = repository.getPlayerAchievements(
                            steamid = steamId,
                            appid = game.appId
                        )
                        achievementsResponse.response.playerStats.achievements?.let { achievements ->
                            nearlyCompleted.addAll(
                                achievements.filter { it.achieved == 0 }.take(2)
                            )
                        }
                    } catch (e: Exception) {
                        // Ignora errori per singoli achievement
                    }
                }

                val completionRate = if (games.isNotEmpty() && nearlyCompleted.isNotEmpty()) {
                    val completed = nearlyCompleted.count { it.achieved == 1 }
                    (completed.toFloat() / nearlyCompleted.size * 100).coerceAtMost(100f)
                } else {
                    0f
                }

                _uiState.value = DashboardUiState.Success(
                    DashboardData(
                        steamId = steamId,
                        playerName = player.personaName,
                        avatarUrl = player.avatarFull,
                        totalPlaytimeHours = totalPlaytimeHours,
                        totalGames = gamesResponse.response.gameCount,
                        recentGames = recentGames,
                        nearlyCompletedAchievements = nearlyCompleted.take(5),
                        completionRate = completionRate,
                        isMockData = RepositoryFactory.isMockMode()
                    )
                )

            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    message = if (RepositoryFactory.isMockMode()) {
                        "Errore nei dati mock: ${e.message}"
                    } else {
                        "Errore API Steam: ${e.message}"
                    }
                )
            }
        }
    }

    fun toggleMockMode(enabled: Boolean) {
        RepositoryFactory.toggleMockMode(enabled)
        _isMockMode.value = enabled
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

data class DashboardData(
    val steamId: String,
    val playerName: String,
    val avatarUrl: String,
    val totalPlaytimeHours: Float,
    val totalGames: Int,
    val recentGames: List<SteamGame>,
    val nearlyCompletedAchievements: List<SteamAchievement>,
    val completionRate: Float = 0f,
    val isMockData: Boolean
)