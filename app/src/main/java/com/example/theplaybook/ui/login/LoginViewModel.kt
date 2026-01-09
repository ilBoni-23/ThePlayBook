package com.example.theplaybook.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theplaybook.ThePlayBookApp
import com.example.theplaybook.auth.SteamAuthManager
import com.example.theplaybook.repository.GameRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authManager by lazy {
        SteamAuthManager(ThePlayBookApp.instance)
    }

    private val repository by lazy {
        ThePlayBookApp.instance.gameRepository
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun checkIfUserIsLoggedIn() {
        viewModelScope.launch {
            val savedSteamId = repository.getSavedSteamId()

            if (savedSteamId != null) {
                _loginState.value = LoginState.LoggedIn(savedSteamId)
            }
        }
    }

    fun loginWithSteam() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            when (val result = authManager.signInWithSteam()) {
                is kotlin.Result.Success -> {
                    val steamId = result.getOrNull()

                    if (steamId != null) {
                        // Sincronizza dati dopo login
                        syncUserData(steamId)
                    } else {
                        _loginState.value = LoginState.Error("Login fallito")
                    }
                }

                is kotlin.Result.Failure -> {
                    _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Errore sconosciuto")
                }
            }
        }
    }

    private suspend fun syncUserData(steamId: String) {
        try {
            val result = repository.syncUserData(steamId)

            if (result.isSuccess) {
                _loginState.value = LoginState.Success(steamId)
            } else {
                _loginState.value = LoginState.Error("Impossibile sincronizzare dati")
            }
        } catch (e: Exception) {
            _loginState.value = LoginState.Error(e.message ?: "Errore di sincronizzazione")
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val steamId: String) : LoginState()
    data class Error(val message: String) : LoginState()
    data class LoggedIn(val steamId: String) : LoginState()
}