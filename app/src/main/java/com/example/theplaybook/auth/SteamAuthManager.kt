package com.example.theplaybook.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

class SteamAuthManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val STEAM_PROVIDER_ID = "oidc.steam"
        const val STEAM_OPENID_URL = "https://steamcommunity.com/openid/login"
    }

    suspend fun signInWithSteam(): Result<String> {
        return try {
            val provider = OAuthProvider.newBuilder(STEAM_PROVIDER_ID)

            // Configura parametri OpenID per Steam
            provider.setCustomParameters(mapOf(
                "openid.ns" to "http://specs.openid.net/auth/2.0",
                "openid.mode" to "checkid_setup",
                "openid.return_to" to "https://theplaybook.app/auth/callback",
                "openid.realm" to "https://theplaybook.app",
                "openid.identity" to "http://specs.openid.net/auth/2.0/identifier_select",
                "openid.claimed_id" to "http://specs.openid.net/auth/2.0/identifier_select"
            ))

            // Avvia il processo di autenticazione
            val result = auth.signInWithProvider(provider.build()).await()

            // Estrai Steam ID dal provider data
            val steamId = extractSteamIdFromUser(result.user)

            if (steamId != null) {
                Result.success(steamId)
            } else {
                Result.failure(Exception("Impossibile ottenere Steam ID"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractSteamIdFromUser(user: com.google.firebase.auth.FirebaseUser?): String? {
        // Steam ID è spesso nell'UID o nei provider data
        val uid = user?.uid ?: return null

        // Estrai Steam ID dall'UID (formato: steam:STEAM_ID)
        return if (uid.startsWith("steam:")) {
            uid.substring(6)
        } else {
            uid
        }
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}