package com.example.theplaybook.data

import com.example.theplaybook.data.mock.MockSteamRepository
import com.example.theplaybook.data.remote.SteamApiService

object RepositoryFactory {

    private var useMock = true // Cambia a false quando avrai API key

    fun createSteamRepository(): SteamApiService {
        return if (useMock) {
            MockSteamRepository()
        } else {
            // Qui instanzi Retrofit con API reale
            // Retrofit.Builder()...
            throw NotImplementedError("API reale non ancora implementata")
        }
    }

    fun toggleMockMode(enabled: Boolean) {
        useMock = enabled
    }

    fun isMockMode() = useMock
}