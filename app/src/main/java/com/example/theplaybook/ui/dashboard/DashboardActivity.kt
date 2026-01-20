package com.example.theplaybook.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import com.example.theplaybook.MainActivity
import com.example.theplaybook.R
import com.example.theplaybook.data.mock.MockPlayer
import com.example.theplaybook.databinding.ActivityDashboardBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    // View references
    private lateinit var tvPlayerName: TextView
    private lateinit var tvSteamId: TextView
    private lateinit var tvTotalHours: TextView
    private lateinit var tvTotalGames: TextView
    private lateinit var tvCompletion: TextView
    private lateinit var tvRecentGames: TextView
    private lateinit var tvNearlyComplete: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var switchMockMode: SwitchCompat
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnGames: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard) // Usa il NUOVO layout

        // Ottieni Steam ID o usa demo
        val steamId = intent.getStringExtra("STEAM_ID")
            ?: MockPlayer.PLAYER_1.steamId

        val demoName = intent.getStringExtra("DEMO_NAME")

        // Inizializza le view
        initViews()

        // Setup UI
        setupUI()

        // Setup observers
        setupObservers()

        // Carica dati
        viewModel.loadDashboardData(steamId)

        // Se c'è un nome demo, usalo
        if (!demoName.isNullOrEmpty()) {
            tvPlayerName.text = demoName
        }
    }

    private fun initViews() {
        tvPlayerName = findViewById(R.id.tvPlayerName)
        tvSteamId = findViewById(R.id.tvSteamId)
        tvTotalHours = findViewById(R.id.tvTotalHours)
        tvTotalGames = findViewById(R.id.tvTotalGames)
        tvCompletion = findViewById(R.id.tvCompletion)
        tvRecentGames = findViewById(R.id.tvRecentGames)
        tvNearlyComplete = findViewById(R.id.tvNearlyComplete)
        ivAvatar = findViewById(R.id.ivAvatar)
        switchMockMode = findViewById(R.id.switchMockMode)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)
        btnGames = findViewById(R.id.btnGames)
        btnCalendar = findViewById(R.id.btnCalendar)
        progressBar = findViewById(R.id.progressBar)
        scrollView = findViewById(R.id.scrollView)
    }

    private fun setupUI() {
        // Tasto Indietro
        btnBack.setOnClickListener {
            finish()
        }

        // Switch Mock Mode
        switchMockMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleMockMode(isChecked)
            Snackbar.make(
                findViewById(R.id.root),
                if (isChecked) "Modalità Mock attiva" else "Modalità Mock disattiva",
                Snackbar.LENGTH_SHORT
            ).show()

            val steamId = intent.getStringExtra("STEAM_ID")
                ?: MockPlayer.PLAYER_1.steamId
            viewModel.loadDashboardData(steamId)
        }

        // Bottone Aggiorna
        btnRefresh.setOnClickListener {
            val steamId = intent.getStringExtra("STEAM_ID")
                ?: MockPlayer.PLAYER_1.steamId
            viewModel.loadDashboardData(steamId)
            Snackbar.make(
                findViewById(R.id.root),
                "Aggiornamento dati...",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Bottom Navigation
        btnHome.setOnClickListener {
            // Già nella home, non fare nulla
            Snackbar.make(
                findViewById(R.id.root),
                "Sei già nella Home",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        btnGames.setOnClickListener {
            Snackbar.make(
                findViewById(R.id.root),
                "Schermata Giochi in sviluppo",
                Snackbar.LENGTH_SHORT
            ).show()
            // TODO: Aprire GiochiActivity quando sarà convertita
        }

        btnCalendar.setOnClickListener {
            Snackbar.make(
                findViewById(R.id.root),
                "Schermata Calendario in sviluppo",
                Snackbar.LENGTH_SHORT
            ).show()
            // TODO: Aprire CalendarioActivity quando sarà convertita
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DashboardUiState.Loading -> showLoading(true)
                is DashboardUiState.Success -> {
                    showLoading(false)
                    updateUI(state.data)
                }
                is DashboardUiState.Error -> {
                    showLoading(false)
                    Snackbar.make(
                        findViewById(R.id.root),
                        state.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.isMockMode.observe(this) { isMock ->
            switchMockMode.isChecked = isMock
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.isVisible = show
        scrollView.isVisible = !show
    }

    private fun updateUI(data: DashboardData) {
        // Header
        tvPlayerName.text = data.playerName
        tvSteamId.text = "Steam ID: ${data.steamId}"

        // Statistiche
        tvTotalHours.text = "%.1f".format(data.totalPlaytimeHours)
        tvTotalGames.text = data.totalGames.toString()

        // Completion (calcolo semplificato)
        val completionRate = if (data.totalGames > 0) {
            (data.nearlyCompletedAchievements.size * 20).coerceAtMost(100)
        } else 0
        tvCompletion.text = "$completionRate%"

        // Giochi recenti
        updateRecentGames(data.recentGames)

        // Achievement
        updateNearlyCompleted(data.nearlyCompletedAchievements)

        // TODO: Caricare immagine avatar quando disponibile
        // ivAvatar.setImageURI(data.avatarUrl)
    }

    private fun updateRecentGames(games: List<com.example.theplaybook.data.remote.models.SteamGame>) {
        tvRecentGames.text = if (games.isEmpty()) {
            getString(R.string.no_recent_games)
        } else {
            games.take(5).joinToString("\n") { game ->
                "• ${game.name} (${game.playtimeForever / 60}h)"
            }
        }
    }

    private fun updateNearlyCompleted(achievements: List<com.example.theplaybook.data.remote.models.SteamAchievement>) {
        tvNearlyComplete.text = if (achievements.isEmpty()) {
            getString(R.string.no_nearly_completed)
        } else {
            achievements.take(5).joinToString("\n") { achievement ->
                "🎯 ${achievement.name} (${achievement.description ?: "Nessuna descrizione"})"
            }
        }
    }

    // Funzione logout (puoi collegarla a un menu)
    private fun logout() {
        Snackbar.make(
            findViewById(R.id.root),
            "Disconnessione...",
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

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}