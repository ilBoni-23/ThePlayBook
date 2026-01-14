package com.example.theplaybook.auth.steam

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SteamAuthHelper(private val activity: Activity) {

    private lateinit var steamLoginLauncher: ActivityResultLauncher<Intent>

    init {
        setupSteamLoginLauncher()
    }

    private fun setupSteamLoginLauncher() {
        steamLoginLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val steamId = result.data?.getStringExtra("steam_id")
                // Gestisci il risultato
            }
        }
    }

    /**
     * Avvia il flusso di login Steam
     */
    fun startSteamLogin() {
        val intent = Intent(activity, SteamAuthActivity::class.java)
        steamLoginLauncher.launch(intent)
    }

    /**
     * Versione semplificata per testing
     */
    suspend fun loginWithSteamSimple(): Result<String> {
        return try {
            // Simula login per testing
            val steamId = "76561197960287930" // Steam ID di esempio

            // Per testing con API reale, dovresti usare:
            // val steamLogin = SteamWebLogin(activity)
            // val result = steamLogin.login()

            Result.success(steamId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}