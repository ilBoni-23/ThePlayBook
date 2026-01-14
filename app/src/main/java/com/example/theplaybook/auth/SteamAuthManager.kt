package com.example.theplaybook.auth

import android.content.Context
import kotlinx.coroutines.delay

class SteamAuthManager(private val context: Context) {

    // Versione mock semplice per testing
    suspend fun signInWithSteam(): Result<String> {
        return try {
            // Simula ritardo di rete
            delay(1500)

            // Genera Steam ID fittizio
            val steamId = "7656119${(80000000..99999999).random()}"
            val steamName = "Player${(1000..9999).random()}"

            // Salva nei preferences
            val prefs = context.getSharedPreferences("theplaybook_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("steam_id", steamId)
                .putString("steam_name", steamName)
                .apply()

            Result.success(steamId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = null

    suspend fun signOut() {
        val prefs = context.getSharedPreferences("theplaybook_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("steam_id")
            .remove("steam_name")
            .apply()
    }

    fun isUserLoggedIn(): Boolean {
        val prefs = context.getSharedPreferences("theplaybook_prefs", Context.MODE_PRIVATE)
        return prefs.getString("steam_id", null) != null
    }
}