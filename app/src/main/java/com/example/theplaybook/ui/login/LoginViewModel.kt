/*package com.example.theplaybook.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theplaybook.repository.GameRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val steamAuthHelper: SteamAuthHelper,
    private val repository: GameRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun loginWithSteam() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            when (val result = steamAuthHelper.loginWithSteamSimple()) {
                is Result.Success -> {
                    val steamId = result.getOrNull()
                    if (steamId != null) {
                        // Sincronizza dati Steam
                        syncSteamData(steamId)
                    } else {
                        _loginState.value = LoginState.Error("Login fallito")
                    }
                }
                is Result.Failure -> {
                    _loginState.value = LoginState.Error(
                        result.exceptionOrNull()?.message ?: "Errore sconosciuto"
                    )
                }
            }
        }
    }

    private suspend fun syncSteamData(steamId: String) {
        try {
            // Prova a sincronizzare dati reali
            val apiResult = repository.syncUserData(steamId)

            if (apiResult.isSuccess) {
                _loginState.value = LoginState.Success(steamId)
            } else {
                // Se l'API fallisce, carica dati demo
                loadDemoDataWithSteamId(steamId)
            }

        } catch (e: Exception) {
            // Fallback a demo
            loadDemoDataWithSteamId(steamId)
        }
    }

    private suspend fun loadDemoDataWithSteamId(steamId: String) {
        val demoData = repository.getMockDashboardData().copy(
            steamId = steamId,
            steamName = "Steam User"
        )
        _loginState.value = LoginState.DemoMode(demoData)
    }
}*/