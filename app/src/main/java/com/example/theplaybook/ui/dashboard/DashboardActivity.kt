package com.example.theplaybook.ui.dashboard

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.theplaybook.databinding.ActivityDashboardBinding
import com.example.theplaybook.data.mock.MockPlayer
import com.google.android.material.snackbar.Snackbar

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val steamId = intent.getStringExtra("STEAM_ID")
            ?: MockPlayer.PLAYER_1.steamId

        setupUI()
        setupObservers()
        viewModel.loadDashboardData(steamId)
    }

    private fun setupUI() {
        binding.switchMockMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleMockMode(isChecked)
            Snackbar.make(binding.root,
                if (isChecked) "Modalità Mock attiva" else "Modalità Mock disattiva",
                Snackbar.LENGTH_SHORT
            ).show()

            val steamId = intent.getStringExtra("STEAM_ID")
                ?: MockPlayer.PLAYER_1.steamId
            viewModel.loadDashboardData(steamId)
        }

        binding.btnRefresh.setOnClickListener {
            val steamId = intent.getStringExtra("STEAM_ID")
                ?: MockPlayer.PLAYER_1.steamId
            viewModel.loadDashboardData(steamId)
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
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isMockMode.observe(this) { isMock ->
            binding.tvMockIndicator.isVisible = isMock
            binding.switchMockMode.isChecked = isMock
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.contentGroup.isVisible = !show
    }

    private fun updateUI(data: DashboardData) {
        binding.tvPlayerName.text = data.playerName
        binding.tvTotalHours.text = "%.1f h".format(data.totalPlaytimeHours)
        binding.tvTotalGames.text = data.totalGames.toString()

        // Mock indicator
        if (data.isMockData) {
            binding.tvMockIndicator.text = "🎮 Dati Mock"
            binding.tvMockIndicator.isVisible = true
        }

        // Mostra giochi recenti
        updateRecentGames(data.recentGames)

        // Mostra achievement quasi completati
        updateNearlyCompleted(data.nearlyCompletedAchievements)
    }

    private fun updateRecentGames(games: List<SteamGame>) {
        binding.tvRecentGames.text = if (games.isEmpty()) {
            "Nessun gioco recente"
        } else {
            games.joinToString("\n") { game ->
                "• ${game.name} (${game.playtimeForever / 60}h)"
            }
        }
    }

    private fun updateNearlyCompleted(achievements: List<SteamAchievement>) {
        binding.tvNearlyComplete.text = if (achievements.isEmpty()) {
            "Nessun achievement quasi completato"
        } else {
            achievements.joinToString("\n") { achievement ->
                "🎯 ${achievement.name} (${achievement.description ?: "Nessuna descrizione"})"
            }
        }
    }
}