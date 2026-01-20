package com.example.theplaybook.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.theplaybook.R
import com.example.theplaybook.data.RepositoryFactory
import com.example.theplaybook.data.mock.MockPlayer
import com.example.theplaybook.data.remote.SteamApiService
import com.example.theplaybook.data.remote.models.OwnedGamesResponse
import com.example.theplaybook.data.remote.models.PlayerSummary
import com.example.theplaybook.data.remote.models.SteamAchievement
import com.example.theplaybook.data.remote.models.SteamGame
import com.example.theplaybook.databinding.ActivityDashboardBinding
import com.example.theplaybook.ui.amici.AmiciActivity
import com.example.theplaybook.ui.calendario.CalendarioActivity
import com.example.theplaybook.ui.giochi.GiochiActivity
import com.example.theplaybook.ui.profilo.ProfiloActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var repository: SteamApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inizializza repository
        repository = RepositoryFactory.createSteamRepository()

        // Ottieni Steam ID o usa demo
        val steamId = intent.getStringExtra("STEAM_ID")
            ?: MockPlayer.PLAYER_1.steamId

        val demoName = intent.getStringExtra("DEMO_NAME")

        // Setup UI
        setupUI()

        // Setup observers per ViewModel
        setupViewModelObservers()

        // Carica dati
        caricaDatiCompleta(steamId, demoName)
    }

    private fun setupUI() {
        // Tasto Indietro
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Avatar click -> Profilo
        binding.ivAvatar.setOnClickListener {
            val intent = Intent(this, ProfiloActivity::class.java)
            startActivity(intent)
        }

        // Switch Mock Mode
        binding.switchMockMode.setOnCheckedChangeListener { _, isChecked ->
            RepositoryFactory.toggleMockMode(isChecked)
            repository = RepositoryFactory.createSteamRepository()

            Snackbar.make(
                binding.root,
                if (isChecked) "🎮 Modalità Mock attiva" else "🌐 Modalità Reale attiva",
                Snackbar.LENGTH_SHORT
            ).show()

            // Ricarica dati
            val steamId = intent.getStringExtra("STEAM_ID") ?: MockPlayer.PLAYER_1.steamId
            val demoName = intent.getStringExtra("DEMO_NAME")
            caricaDatiCompleta(steamId, demoName)
        }

        // Bottone Aggiorna
        binding.btnRefresh.setOnClickListener {
            val steamId = intent.getStringExtra("STEAM_ID") ?: MockPlayer.PLAYER_1.steamId
            val demoName = intent.getStringExtra("DEMO_NAME")
            caricaDatiCompleta(steamId, demoName)

            Snackbar.make(
                binding.root,
                "🔄 Aggiornamento dati...",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Bottom Navigation
        binding.btnHome.setOnClickListener {
            // Se siamo già in Dashboard, ricarica i dati
            val steamId = intent.getStringExtra("STEAM_ID") ?: MockPlayer.PLAYER_1.steamId
            val demoName = intent.getStringExtra("DEMO_NAME")
            caricaDatiCompleta(steamId, demoName)

            Snackbar.make(
                binding.root,
                "🏠 Dashboard aggiornata",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.btnGames.setOnClickListener {
            val intent = Intent(this, GiochiActivity::class.java)
            startActivity(intent)
        }

        binding.btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewModelObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DashboardUiState.Loading -> showLoading(true)
                is DashboardUiState.Success -> {
                    showLoading(false)
                    aggiornaUIDaViewModel(state.data)
                }
                is DashboardUiState.Error -> {
                    showLoading(false)
                    // Mostra errore con Snackbar invece di TextView
                    Snackbar.make(
                        binding.root,
                        "⚠️ ${state.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.isMockMode.observe(this) { isMock ->
            binding.switchMockMode.isChecked = isMock
        }
    }

    // ==================== METODO PRINCIPALE PER CARICARE DATI ====================

    private fun caricaDatiCompleta(steamId: String, demoName: String? = null) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1. Carica profilo utente
                val playerData = caricaProfiloUtente(steamId)

                // 2. Carica giochi posseduti
                val gamesData = caricaGiochi(steamId)

                // 3. Calcola statistiche
                val totalPlaytimeHours = gamesData.games.sumOf { it.playtimeForever } / 60f
                val recentGames = gamesData.games
                    .filter { it.rtimeLastPlayed != null }
                    .sortedByDescending { it.rtimeLastPlayed }
                    .take(5)

                // 4. Carica achievement per primi 2 giochi
                val achievementsData = caricaAchievements(steamId, gamesData.games.take(2))

                // 5. Calcola completion rate
                val completionRate = calcolaCompletionRate(achievementsData)

                // 6. Aggiorna UI
                aggiornaUICompleta(
                    playerData = playerData,
                    totalPlaytimeHours = totalPlaytimeHours,
                    totalGames = gamesData.gameCount,
                    recentGames = recentGames,
                    achievements = achievementsData.take(5),
                    completionRate = completionRate,
                    demoName = demoName,
                    isMockMode = RepositoryFactory.isMockMode()
                )

                showLoading(false)

                Snackbar.make(
                    binding.root,
                    "✅ Dati caricati con successo!",
                    Snackbar.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                showLoading(false)
                Snackbar.make(
                    binding.root,
                    "❌ Errore: ${e.localizedMessage}",
                    Snackbar.LENGTH_LONG
                ).show()

                // Fallback a dati mock
                caricaDatiFallback(steamId, demoName)
            }
        }
    }

    // ==================== METODI AUSILIARI ====================

    private suspend fun caricaProfiloUtente(steamId: String): PlayerSummary {
        return withContext(Dispatchers.IO) {
            try {
                val response = repository.getPlayerSummaries(steamId)
                if (response.response.players.isNotEmpty()) {
                    response.response.players.first()
                } else {
                    // Fallback
                    MockPlayer.PLAYER_1.copy(steamId = steamId)
                }
            } catch (e: Exception) {
                // Fallback in caso di errore
                MockPlayer.PLAYER_1.copy(
                    steamId = steamId,
                    personaName = "Utente Steam"
                )
            }
        }
    }

    private suspend fun caricaGiochi(steamId: String): OwnedGamesResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = repository.getOwnedGames(steamId)
                response.response
            } catch (e: Exception) {
                // Fallback a dati mock limitati
                OwnedGamesResponse(
                    gameCount = 8,
                    games = MockGames.ALL_GAMES.take(8)
                )
            }
        }
    }

    private suspend fun caricaAchievements(
        steamId: String,
        games: List<SteamGame>
    ): List<SteamAchievement> {
        return withContext(Dispatchers.IO) {
            val achievements = mutableListOf<SteamAchievement>()

            games.forEach { game ->
                try {
                    val response = repository.getPlayerAchievements(steamId, game.appId)
                    response.response.playerStats.achievements?.let { gameAchievements ->
                        // Prendi 3 achievement non sbloccati (achieved = 0) per gioco
                        achievements.addAll(gameAchievements.filter { it.achieved == 0 }.take(3))
                    }
                } catch (e: Exception) {
                    // Ignora errori per singoli giochi
                }
            }

            achievements
        }
    }

    private fun calcolaCompletionRate(achievements: List<SteamAchievement>): Float {
        return if (achievements.isNotEmpty()) {
            val total = achievements.size
            val completed = achievements.count { it.achieved == 1 }
            (completed.toFloat() / total * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    private fun caricaDatiFallback(steamId: String, demoName: String?) {
        // Usa dati mock completi in caso di fallimento
        val mockPlayer = MockPlayer.PLAYER_1.copy(steamId = steamId)
        val mockGames = MockGames.ALL_GAMES.take(10)
        val mockAchievements = MockAchievements.getForGame(730L).take(5)

        aggiornaUICompleta(
            playerData = mockPlayer,
            totalPlaytimeHours = 342.5f,
            totalGames = mockGames.size,
            recentGames = mockGames.take(5),
            achievements = mockAchievements,
            completionRate = 65.5f,
            demoName = demoName,
            isMockMode = true
        )

        Snackbar.make(
            binding.root,
            "⚠️ Usando dati demo",
            Snackbar.LENGTH_LONG
        ).show()
    }

    // ==================== AGGIORNAMENTO UI ====================

    private fun aggiornaUICompleta(
        playerData: PlayerSummary,
        totalPlaytimeHours: Float,
        totalGames: Int,
        recentGames: List<SteamGame>,
        achievements: List<SteamAchievement>,
        completionRate: Float,
        demoName: String?,
        isMockMode: Boolean
    ) {
        // 1. Header - Profilo
        binding.tvPlayerName.text = demoName ?: playerData.personaName
        binding.tvSteamId.text = "Steam ID: ${playerData.steamId.take(12)}..."

        // 2. Avatar
        caricaAvatar(playerData.avatarFull)

        // 3. Statistiche principali
        binding.tvTotalHours.text = "%.1f".format(totalPlaytimeHours)
        binding.tvTotalGames.text = totalGames.toString()
        binding.tvCompletion.text = "%.1f%%".format(completionRate)

        // 4. Giochi recenti
        aggiornaGiochiRecenti(recentGames)

        // 5. Achievement quasi completati
        aggiornaAchievement(achievements)

        // 6. Indicatore modalità
        aggiornaIndicatoreModalita(isMockMode)
    }

    private fun aggiornaUIDaViewModel(data: DashboardData) {
        // Versione che usa i dati dal ViewModel
        binding.tvPlayerName.text = data.playerName
        binding.tvSteamId.text = "Steam ID: ${data.steamId.take(12)}..."
        binding.tvTotalHours.text = "%.1f".format(data.totalPlaytimeHours)
        binding.tvTotalGames.text = data.totalGames.toString()
        binding.tvCompletion.text = "%.1f%%".format(data.completionRate)

        caricaAvatar(data.avatarUrl)
        aggiornaGiochiRecenti(data.recentGames)
        aggiornaAchievement(data.nearlyCompletedAchievements)
        aggiornaIndicatoreModalita(data.isMockData)
    }

    private fun caricaAvatar(avatarUrl: String) {
        if (avatarUrl.isNotEmpty()) {
            try {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } catch (e: Exception) {
                binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    private fun aggiornaGiochiRecenti(games: List<SteamGame>) {
        binding.tvRecentGames.text = if (games.isEmpty()) {
            "Nessun gioco recente"
        } else {
            games.joinToString("\n") { game ->
                "🎮 ${game.name}\n   ⏱️ ${game.playtimeForever / 60}h"
            }
        }
    }

    private fun aggiornaAchievement(achievements: List<SteamAchievement>) {
        binding.tvNearlyComplete.text = if (achievements.isEmpty()) {
            "Nessun achievement in progress"
        } else {
            achievements.take(5).joinToString("\n") { achievement ->
                val icon = if (achievement.achieved == 1) "✅" else "🎯"
                val status = if (achievement.achieved == 1) "Completato" else "Da completare"
                "$icon ${achievement.name}\n   📝 $status"
            }
        }
    }

    private fun aggiornaIndicatoreModalita(isMockMode: Boolean) {
        binding.switchMockMode.isChecked = isMockMode
        binding.switchMockMode.text = if (isMockMode) "Mock Mode ON" else "Mock Mode OFF"
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.scrollView.isVisible = !show
    }

    // ==================== LOGOUT ====================

    private fun logout() {
        Snackbar.make(
            binding.root,
            "Disconnessione in corso...",
            Snackbar.LENGTH_SHORT
        ).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authManager = com.example.theplaybook.auth.SteamAuthManager(this@DashboardActivity)
                authManager.signOut()
            } catch (e: Exception) {
                // Ignora
            }
        }

        // Opzione A: Se MainActivity esiste nella directory principale
        val intent = Intent(this, com.example.theplaybook.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // ==================== LIFECYCLE ====================

    override fun onResume() {
        super.onResume()
        // Aggiorna dati quando si ritorna all'activity
        if (!binding.progressBar.isVisible) {
            val steamId = intent.getStringExtra("STEAM_ID") ?: MockPlayer.PLAYER_1.steamId
            val demoName = intent.getStringExtra("DEMO_NAME")
            caricaDatiCompleta(steamId, demoName)
        }
    }
}

// ==================== CLASSI AUSILIARIE LOCALI ====================

object MockGames {
    val ALL_GAMES = listOf(
        SteamGame(730, "Counter-Strike 2", 1250, 65, "icon1", "logo1", true, System.currentTimeMillis()/1000 - 86400),
        SteamGame(570, "Dota 2", 890, 42, "icon2", "logo2", true, System.currentTimeMillis()/1000 - 172800),
        SteamGame(271590, "GTA V", 456, 23, "icon3", "logo3", true, System.currentTimeMillis()/1000 - 2592000),
        SteamGame(1245620, "Elden Ring", 234, 78, "icon4", "logo4", true, System.currentTimeMillis()/1000 - 345600),
        SteamGame(1091500, "Cyberpunk 2077", 189, 45, "icon5", "logo5", true, System.currentTimeMillis()/1000 - 1728000)
    )
}

object MockAchievements {
    fun getForGame(appId: Long): List<SteamAchievement> {
        return listOf(
            SteamAchievement("ACH_1", 1, System.currentTimeMillis()/1000 - 864000, "First Blood", "Get first kill"),
            SteamAchievement("ACH_2", 0, null, "Master", "Complete all levels"),
            SteamAchievement("ACH_3", 1, System.currentTimeMillis()/1000 - 1728000, "Explorer", "Discover all areas"),
            SteamAchievement("ACH_4", 0, null, "Speedrun", "Finish under 10 hours"),
            SteamAchievement("ACH_5", 1, System.currentTimeMillis()/1000 - 2592000, "Collector", "Get all items")
        )
    }
}