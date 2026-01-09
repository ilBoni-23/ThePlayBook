package com.example.theplaybook.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theplaybook.ThePlayBookApp
import com.example.theplaybook.auth.SteamAuthManager
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository by lazy {
        ThePlayBookApp.instance.gameRepository
    }

    private val authManager by lazy {
        SteamAuthManager(ThePlayBookApp.instance)
    }

    private val _dashboardState = MutableLiveData<DashboardState>(DashboardState.Loading)
    val dashboardState: LiveData<DashboardState> = _dashboardState

    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading

            try {
                // Prima prova a caricare dalla cache
                val cachedData = repository.getCachedDashboardData()

                if (cachedData != null) {
                    _dashboardState.value = DashboardState.Success(cachedData)

                    // Poi sincronizza in background
                    syncFreshData()
                } else {
                    // Nessun dato in cache, sincronizza ora
                    syncFreshData()
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Errore nel caricamento dati")
            }
        }
    }

    private suspend fun syncFreshData() {
        val steamId = repository.getSavedSteamId()

        if (steamId != null) {
            when (val result = repository.syncUserData(steamId)) {
                is kotlin.Result.Success -> {
                    _dashboardState.value = DashboardState.Success(result.getOrDefault(
                        repository.getCachedDashboardData() ?: throw IllegalStateException("Dati non disponibili")
                    ))
                }

                is kotlin.Result.Failure -> {
                    // Mostra errore ma mantieni dati in cache
                    _dashboardState.value = DashboardState.Error("Aggiornamento fallito: ${result.exceptionOrNull()?.message}")
                }
            }
        } else {
            _dashboardState.value = DashboardState.Error("Utente non autenticato")
        }
    }

    fun refreshData() {
        loadDashboardData()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                repository.clearAllData()
                _dashboardState.value = DashboardState.Logout
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error("Logout fallito")
            }
        }
    }
}

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardData) : DashboardState()
    data class Error(val message: String) : DashboardState()
    object Logout : DashboardState()
}