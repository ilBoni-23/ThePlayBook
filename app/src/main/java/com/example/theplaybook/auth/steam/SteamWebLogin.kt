package com.example.theplaybook.auth.steam

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SteamWebLogin(private val context: Context) {

    companion object {
        private const val STEAM_OPENID_URL = "https://steamcommunity.com/openid/login"

        // Questi devono essere configurati con il tuo dominio
        private const val RETURN_URL = "https://theplaybook.auth/steam/callback"
        private const val REALM = "https://theplaybook.app"

        // Per testing locale puoi usare un URL schema personalizzato
        private const val LOCAL_RETURN_URL = "theplaybook://steam/callback"

        // Pattern per estrarre Steam ID dalla response
        private val STEAM_ID_PATTERN = Regex("https?://steamcommunity\\.com/openid/id/(\\d+)")
    }

    /**
     * Avvia il login Steam via Web
     */
    suspend fun login(): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            // Costruisci URL OpenID
            val openIdParams = buildOpenIdParams()
            val url = buildOpenIdUrl(openIdParams)

            // Avvia Custom Tab
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setShowTitle(true)
                .build()

            // Aggiungi listener per intercettare il callback
            setupCallbackInterceptor(continuation)

            customTabsIntent.launchUrl(context, url)

        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    private fun buildOpenIdParams(): Map<String, String> {
        return mapOf(
            "openid.ns" to "http://specs.openid.net/auth/2.0",
            "openid.mode" to "checkid_setup",
            "openid.return_to" to LOCAL_RETURN_URL, // Usa locale per testing
            "openid.realm" to REALM,
            "openid.identity" to "http://specs.openid.net/auth/2.0/identifier_select",
            "openid.claimed_id" to "http://specs.openid.net/auth/2.0/identifier_select"
        )
    }

    private fun buildOpenIdUrl(params: Map<String, String>): Uri {
        val builder = Uri.parse(STEAM_OPENID_URL).buildUpon()
        params.forEach { (key, value) ->
            builder.appendQueryParameter(key, value)
        }
        return builder.build()
    }

    private fun setupCallbackInterceptor(continuation: Continuation<String>) {
        // Per intercettare il callback, dobbiamo:
        // 1. Configurare un intent filter nel manifest
        // 2. Gestire l'intent quando Steam reindirizza al nostro app
        // Questa è una versione semplificata

        // In una implementazione completa, useresti un BroadcastReceiver
        // o un Activity con intent filter

        // Per ora, simuliamo il callback con Steam ID di test
        Thread {
            Thread.sleep(3000) // Simula attesa login
            continuation.resume("76561197960287930") // Steam ID di esempio
        }.start()
    }

    /**
     * Estrae Steam ID dalla URL di callback
     */
    fun extractSteamIdFromCallback(url: String): String? {
        val uri = Uri.parse(url)
        val claimedId = uri.getQueryParameter("openid.claimed_id") ?: return null

        return STEAM_ID_PATTERN.find(claimedId)?.groupValues?.get(1)
    }

    /**
     * Verifica la response di Steam
     */
    suspend fun verifySteamResponse(params: Map<String, String>): Result<String> {
        return try {
            // Aggiungi il parametro per la verifica
            val verificationParams = params.toMutableMap().apply {
                put("openid.mode", "check_authentication")
            }

            // Invia richiesta di verifica a Steam
            val verificationUrl = buildOpenIdUrl(verificationParams)
            val response = makeHttpRequest(verificationUrl.toString())

            if (response.contains("is_valid:true")) {
                val steamId = extractSteamIdFromParams(params)
                Result.success(steamId)
            } else {
                Result.failure(Exception("Verifica fallita"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractSteamIdFromParams(params: Map<String, String>): String {
        val claimedId = params["openid.claimed_id"] ?: ""
        return STEAM_ID_PATTERN.find(claimedId)?.groupValues?.get(1)
            ?: throw Exception("Steam ID non trovato")
    }

    private suspend fun makeHttpRequest(url: String): String {
        // Implementa chiamata HTTP
        // Usa OkHttp o HttpURLConnection
        return "" // Placeholder
    }
}